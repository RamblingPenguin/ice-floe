package com.ramblingpenguin.icefloe.core;

import com.ramblingpenguin.icefloe.core.context.ContextualNode;
import com.ramblingpenguin.icefloe.core.context.ContextualSequence;
import com.ramblingpenguin.icefloe.core.context.NodeKey;
import com.ramblingpenguin.icefloe.core.context.SequenceContext;
import com.ramblingpenguin.icefloe.core.node.RetryNode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Demonstrates a more complex, multi-step workflow using a ContextualSequence,
 * including conditional logic and retries.
 */
public class ComplexContextualWorkflowTest {

    // --- Data Records for the Workflow ---
    public record OrderRequest(String productId, int quantity, String customerId) {}
    public record ProductDetails(String productId, String productName, BigDecimal price) {}
    public record CustomerDetails(String customerId, String customerName) {}
    public record OrderTotal(BigDecimal total) {}
    public record ApprovalStatus(String status) {}
    public record FinalOrderSummary(String customerName, String productName, int quantity, BigDecimal total, String approvalStatus) {}

    // --- Node Keys for Context Access ---
    private static final NodeKey<OrderRequest> ORDER_REQUEST_KEY = new NodeKey<>("initial", OrderRequest.class);
    private static final NodeKey<ProductDetails> PRODUCT_DETAILS_KEY = new NodeKey<>("product-details", ProductDetails.class);
    private static final NodeKey<CustomerDetails> CUSTOMER_DETAILS_KEY = new NodeKey<>("customer-details", CustomerDetails.class);
    private static final NodeKey<OrderTotal> ORDER_TOTAL_KEY = new NodeKey<>("order-total", OrderTotal.class);
    private static final NodeKey<ApprovalStatus> APPROVAL_STATUS_KEY = new NodeKey<>("approval-status", ApprovalStatus.class);
    private static final NodeKey<FinalOrderSummary> FINAL_SUMMARY_KEY = new NodeKey<>("final-summary", FinalOrderSummary.class);

    @Test
    void testOrderProcessingWorkflow_WithRetryAndPredicate() {
        // 1. Define the nodes (business logic)

        // Simulate a service that might fail a couple of times
        final AtomicInteger customerFetchAttempts = new AtomicInteger(0);
        Node<String, CustomerDetails> flakyFetchCustomerNode = customerId -> {
            if (customerFetchAttempts.incrementAndGet() <= 2) {
                throw new RuntimeException("Transient network error!");
            }
            return new CustomerDetails(customerId, "John Doe");
        };

        // Wrap the flaky node in a RetryNode
        Node<String, CustomerDetails> resilientFetchCustomerNode = new RetryNode<>(flakyFetchCustomerNode, 3);

        Node<String, ProductDetails> fetchProductNode = productId ->
                new ProductDetails(productId, "Super-Widget", new BigDecimal("19.99"));

        Node<SequenceContext, OrderTotal> calculateTotalNode = context -> {
            ProductDetails product = context.get(PRODUCT_DETAILS_KEY).orElseThrow();
            OrderRequest request = context.get(ORDER_REQUEST_KEY).orElseThrow();
            BigDecimal total = product.price().multiply(new BigDecimal(request.quantity()));
            return new OrderTotal(total);
        };

        // Define a PredicateNode for conditional logic based on the context
        Node<SequenceContext, ApprovalStatus> approvalPredicateNode = new PredicateNode<>(
                context -> context.get(ORDER_TOTAL_KEY).orElseThrow().total().compareTo(new BigDecimal("100.00")) > 0,
                context -> new ApprovalStatus("Requires Manual Review"), // Executes if total > 100
                context -> new ApprovalStatus("Auto-Approved")          // Executes if total <= 100
        );

        Node<SequenceContext, FinalOrderSummary> assembleSummaryNode = context -> {
            CustomerDetails customer = context.get(CUSTOMER_DETAILS_KEY).orElseThrow();
            ProductDetails product = context.get(PRODUCT_DETAILS_KEY).orElseThrow();
            OrderTotal total = context.get(ORDER_TOTAL_KEY).orElseThrow();
            OrderRequest request = context.get(ORDER_REQUEST_KEY).orElseThrow();
            ApprovalStatus approval = context.get(APPROVAL_STATUS_KEY).orElseThrow();
            return new FinalOrderSummary(customer.customerName(), product.productName(), request.quantity(), total.total(), approval.status());
        };

        // 2. Build the contextual sequence
        ContextualSequence<OrderRequest> orderProcessingSequence = ContextualSequence.Builder.of(OrderRequest.class)
                .then(ContextualNode.of(
                        PRODUCT_DETAILS_KEY,
                        ctx -> ctx.get(ORDER_REQUEST_KEY).orElseThrow().productId(),
                        fetchProductNode))
                .then(ContextualNode.of(
                        CUSTOMER_DETAILS_KEY,
                        ctx -> ctx.get(ORDER_REQUEST_KEY).orElseThrow().customerId(),
                        resilientFetchCustomerNode)) // Using the resilient node
                .then(ORDER_TOTAL_KEY, calculateTotalNode)
                .then(APPROVAL_STATUS_KEY, approvalPredicateNode) // Adding the predicate node
                .then(FINAL_SUMMARY_KEY, assembleSummaryNode)
                .build();

        // 3. Execute and Assert for a low-value order (should be auto-approved)
        OrderRequest lowValueRequest = new OrderRequest("prod-123", 2, "cust-abc");
        SequenceContext lowValueFinalContext = orderProcessingSequence.apply(lowValueRequest);
        FinalOrderSummary lowValueSummary = lowValueFinalContext.get(FINAL_SUMMARY_KEY).orElseThrow();

        assertNotNull(lowValueSummary);
        assertEquals("John Doe", lowValueSummary.customerName());
        assertEquals(3, customerFetchAttempts.get()); // Check that retry happened
        assertEquals(new BigDecimal("39.98"), lowValueSummary.total());
        assertEquals("Auto-Approved", lowValueSummary.approvalStatus());

        // 4. Execute and Assert for a high-value order (should require manual review)
        customerFetchAttempts.set(0); // Reset for the next run
        OrderRequest highValueRequest = new OrderRequest("prod-123", 10, "cust-abc");
        SequenceContext highValueFinalContext = orderProcessingSequence.apply(highValueRequest);
        FinalOrderSummary highValueSummary = highValueFinalContext.get(FINAL_SUMMARY_KEY).orElseThrow();

        assertNotNull(highValueSummary);
        assertEquals(new BigDecimal("199.90"), highValueSummary.total());
        assertEquals("Requires Manual Review", highValueSummary.approvalStatus());
    }
}

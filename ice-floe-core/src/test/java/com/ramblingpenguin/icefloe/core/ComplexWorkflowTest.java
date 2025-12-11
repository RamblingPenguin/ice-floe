package com.ramblingpenguin.icefloe.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ComplexWorkflowTest {

    // --- Domain Objects ---

    record OrderItem(String name, int price, int quantity) {}

    record Order(String id, boolean isPriority, List<OrderItem> items) {}

    static class OrderResult {
        String orderId;
        int totalCost;
        List<String> processingLog;

        public OrderResult(String orderId, int totalCost, List<String> processingLog) {
            this.orderId = orderId;
            this.totalCost = totalCost;
            this.processingLog = processingLog;
        }

        public OrderResult addLog(String log) {
            this.processingLog.add(log);
            return this;
        }

        public OrderResult addToTotal(int amount) {
            this.totalCost += amount;
            return this;
        }
    }

    // --- Nodes / Functions ---

    // A simple validation step
    static class ValidateOrder implements Node<Order, Order> {
        @Override
        public Order apply(Order order) {
            if (order.items() == null || order.items().isEmpty()) {
                throw new IllegalArgumentException("Order must have items");
            }
            return order;
        }
    }

    // --- Tests ---

    @Test
    public void testComplexOrderProcessingWorkflow() {
        // 1. Define the Priority Path (Simpler, flat fee calculation)
        Node<Order, OrderResult> priorityPath = Sequence.Builder.of(Order.class)
                .then(order -> new OrderResult(order.id(), 0, new java.util.ArrayList<>()))
                .then(result -> result.addLog("Priority Processing Started"))
                .then(result -> result.addToTotal(10)) // Flat processing fee
                // In a real app, this might do something simpler than itemizing
                .then(result -> result.addLog("Expedited Shipping Applied"))
                .build();

        // 2. Define the Standard Path (Parallel item processing)
        // We use a ForkSequence to calculate the cost of items in parallel
        Node<Order, OrderResult> standardItemProcessor = ForkSequence
                .<Order, OrderItem, Integer>builder(
                        Order::items,                  // Split: Order -> List<OrderItem>
                        item -> item.price() * item.quantity() // Fork: Calculate item cost
                )
                .withReducerFactory(
                        // Initializer: Create the result object from the input Order
                        order -> new OrderResult(order.id(), 0, new java.util.ArrayList<>(List.of("Standard Processing Started"))),
                        // Reducer: Accumulate item costs into the result
                        OrderResult::addToTotal
                )
                .parallel()
                .build();
        
        // Wrap the fork in a sequence to add a final step
        Node<Order, OrderResult> standardPath = Sequence.Builder.of(Order.class)
                .then(standardItemProcessor)
                .then(result -> result.addLog("Standard Shipping Applied"))
                .build();

        // 3. Assemble the Main Pipeline
        // Input: Order -> Validation -> (Priority ? PriorityPath : StandardPath) -> Finalize
        Sequence<Order, OrderResult> pipeline = Sequence.Builder.of(Order.class)
                .then(new ValidateOrder())
                .then(new PredicateNode<>(
                        Order::isPriority,
                        priorityPath,
                        standardPath
                ))
                .then(result -> result.addLog("Workflow Complete"))
                .build();


        // --- Execution Case A: Priority Order ---
        Order priorityOrder = new Order("ORD-1", true, List.of(new OrderItem("Apple", 1, 10)));
        OrderResult pResult = pipeline.apply(priorityOrder);

        assertEquals("ORD-1", pResult.orderId);
        assertEquals(10, pResult.totalCost); // Only the flat fee
        assertTrue(pResult.processingLog.contains("Priority Processing Started"));
        assertTrue(pResult.processingLog.contains("Expedited Shipping Applied"));
        assertTrue(pResult.processingLog.contains("Workflow Complete"));


        // --- Execution Case B: Standard Order (Parallel processing) ---
        Order standardOrder = new Order("ORD-2", false, List.of(
                new OrderItem("Book", 15, 2),  // 30
                new OrderItem("Pen", 2, 5)     // 10
        ));
        OrderResult sResult = pipeline.apply(standardOrder);

        assertEquals("ORD-2", sResult.orderId);
        assertEquals(40, sResult.totalCost); // 30 + 10
        assertTrue(sResult.processingLog.contains("Standard Processing Started"));
        assertTrue(sResult.processingLog.contains("Standard Shipping Applied"));
        assertTrue(sResult.processingLog.contains("Workflow Complete"));
    }
}

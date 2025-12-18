package com.ramblingpenguin.icefloe.core.node;

import com.ramblingpenguin.icefloe.core.Node;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryNodeTest {

    record TestRecord(String value) {}

    @Test
    void testExecutesSuccessfullyOnFirstAttempt() {
        Node<TestRecord, TestRecord> node = (input) -> new TestRecord("output");
        RetryNode<TestRecord, TestRecord> retryNode = new RetryNode<>(node, 3);

        TestRecord result = retryNode.apply(new TestRecord("input"));

        assertEquals("output", result.value());
    }

    @Test
    void testExecutesSuccessfullyAfterAFewRetries() {
        AtomicInteger attempts = new AtomicInteger(0);
        Node<TestRecord, TestRecord> node = (input) -> {
            if (attempts.getAndIncrement() < 2) {
                throw new RuntimeException("failed");
            }
            return new TestRecord("output");
        };
        RetryNode<TestRecord, TestRecord> retryNode = new RetryNode<>(node, 3);

        TestRecord result = retryNode.apply(new TestRecord("input"));

        assertEquals("output", result.value());
        assertEquals(3, attempts.get());
    }

    @Test
    void testFailsAfterAllRetries() {
        Node<TestRecord, TestRecord> node = (input) -> {
            throw new RuntimeException("failed");
        };
        RetryNode<TestRecord, TestRecord> retryNode = new RetryNode<>(node, 3);

        assertThrows(RuntimeException.class, () -> retryNode.apply(new TestRecord("input")));
    }
}

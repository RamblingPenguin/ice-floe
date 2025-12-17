package com.ramblingpenguin.icefloe.core.node;

import com.ramblingpenguin.icefloe.core.Node;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryNodeTest {

    @Test
    void testExecutesSuccessfullyOnFirstAttempt() {
        Node<String, String> node = (input) -> "output";
        RetryNode<String, String> retryNode = new RetryNode<>(node, 3);

        String result = retryNode.apply("input");

        assertEquals("output", result);
    }

    @Test
    void testExecutesSuccessfullyAfterAFewRetries() {
        AtomicInteger attempts = new AtomicInteger(0);
        Node<String, String> node = (input) -> {
            if (attempts.getAndIncrement() < 2) {
                throw new RuntimeException("failed");
            }
            return "output";
        };
        RetryNode<String, String> retryNode = new RetryNode<>(node, 3);

        String result = retryNode.apply("input");

        assertEquals("output", result);
        assertEquals(3, attempts.get());
    }

    @Test
    void testFailsAfterAllRetries() {
        Node<String, String> node = (input) -> {
            throw new RuntimeException("failed");
        };
        RetryNode<String, String> retryNode = new RetryNode<>(node, 3);

        assertThrows(RuntimeException.class, () -> retryNode.apply("input"));
    }
}

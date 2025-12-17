package com.ramblingpenguin.icefloe.core.node;

import com.ramblingpenguin.icefloe.core.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FallbackNodeTest {

    @Test
    void testPrimaryNodeSucceeds() {
        Node<String, String> primaryNode = (input) -> "primary";
        Node<String, String> fallbackNode = (input) -> "fallback";
        FallbackNode<String, String> fallback = new FallbackNode<>(primaryNode, fallbackNode);

        String result = fallback.apply("input");

        assertEquals("primary", result);
    }

    @Test
    void testPrimaryNodeFailsAndFallbackSucceeds() {
        Node<String, String> primaryNode = (input) -> {
            throw new RuntimeException("primary failed");
        };
        Node<String, String> fallbackNode = (input) -> "fallback";
        FallbackNode<String, String> fallback = new FallbackNode<>(primaryNode, fallbackNode);

        String result = fallback.apply("input");

        assertEquals("fallback", result);
    }

    @Test
    void testPrimaryNodeFailsAndShouldUseFallbackIsFalse() {
        Node<String, String> primaryNode = (input) -> {
            throw new RuntimeException("primary failed");
        };
        Node<String, String> fallbackNode = (input) -> "fallback";
        FallbackNode<String, String> fallback = new FallbackNode<>(primaryNode, fallbackNode) {
            @Override
            public java.util.function.Predicate<Throwable> shouldUseFallback() {
                return (t) -> false;
            }
        };

        assertThrows(RuntimeException.class, () -> fallback.apply("input"));
    }
}

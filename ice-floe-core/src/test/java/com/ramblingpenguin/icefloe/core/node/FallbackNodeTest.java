package com.ramblingpenguin.icefloe.core.node;

import com.ramblingpenguin.icefloe.core.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FallbackNodeTest {

    record TestRecord(String value) {}

    @Test
    void testPrimaryNodeSucceeds() {
        Node<TestRecord, TestRecord> primaryNode = (input) -> new TestRecord("primary");
        Node<TestRecord, TestRecord> fallbackNode = (input) -> new TestRecord("fallback");
        FallbackNode<TestRecord, TestRecord> fallback = new FallbackNode<>(primaryNode, fallbackNode);

        TestRecord result = fallback.apply(new TestRecord("input"));

        assertEquals("primary", result.value());
    }

    @Test
    void testPrimaryNodeFailsAndFallbackSucceeds() {
        Node<TestRecord, TestRecord> primaryNode = (input) -> {
            throw new RuntimeException("primary failed");
        };
        Node<TestRecord, TestRecord> fallbackNode = (input) -> new TestRecord("fallback");
        FallbackNode<TestRecord, TestRecord> fallback = new FallbackNode<>(primaryNode, fallbackNode);

        TestRecord result = fallback.apply(new TestRecord("input"));

        assertEquals("fallback", result.value());
    }

    @Test
    void testPrimaryNodeFailsAndShouldUseFallbackIsFalse() {
        Node<TestRecord, TestRecord> primaryNode = (input) -> {
            throw new RuntimeException("primary failed");
        };
        Node<TestRecord, TestRecord> fallbackNode = (input) -> new TestRecord("fallback");
        FallbackNode<TestRecord, TestRecord> fallback = new FallbackNode<>(primaryNode, fallbackNode, t -> false);

        assertThrows(RuntimeException.class, () -> fallback.apply(new TestRecord("input")));
    }
}

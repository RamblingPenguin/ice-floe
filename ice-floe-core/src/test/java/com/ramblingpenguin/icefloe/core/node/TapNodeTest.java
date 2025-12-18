package com.ramblingpenguin.icefloe.core.node;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TapNodeTest {

    record TestRecord(String value) {}

    @Test
    void testTap() {
        AtomicReference<TestRecord> tappedValue = new AtomicReference<>();
        Consumer<TestRecord> tap = tappedValue::set;
        TapNode<TestRecord> tapNode = new TapNode<>(tap);

        TestRecord result = tapNode.apply(new TestRecord("input"));

        assertEquals("input", result.value());
        assertEquals("input", tappedValue.get().value());
    }
}

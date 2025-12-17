package com.ramblingpenguin.icefloe.core.node;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TapNodeTest {

    @Test
    void testTap() {
        AtomicReference<String> tappedValue = new AtomicReference<>();
        Consumer<String> tap = tappedValue::set;
        TapNode<String> tapNode = new TapNode<>(tap);

        String result = tapNode.apply("input");

        assertEquals("input", result);
        assertEquals("input", tappedValue.get());
    }
}

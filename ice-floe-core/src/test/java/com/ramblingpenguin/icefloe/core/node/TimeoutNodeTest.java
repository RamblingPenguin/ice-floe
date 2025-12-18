package com.ramblingpenguin.icefloe.core.node;

import com.ramblingpenguin.icefloe.core.Node;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeoutNodeTest {

    record TestRecord(String value) {}

    @Test
    void testNodeExecutesWithinTimeout() {
        Node<TestRecord, TestRecord> node = (input) -> new TestRecord("output");
        TimeoutNode<TestRecord, TestRecord> timeoutNode = new TimeoutNode<>(node, 1, TimeUnit.SECONDS);

        TestRecord result = timeoutNode.apply(new TestRecord("input"));

        assertEquals("output", result.value());
    }

    @Test
    void testNodeExceedsTimeout() {
        Node<TestRecord, TestRecord> node = (input) -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new TestRecord("output");
        };
        TimeoutNode<TestRecord, TestRecord> timeoutNode = new TimeoutNode<>(node, 1, TimeUnit.SECONDS);

        assertThrows(RuntimeException.class, () -> timeoutNode.apply(new TestRecord("input")));
    }
}

package com.ramblingpenguin.icefloe.core.node;

import com.ramblingpenguin.icefloe.core.Node;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeoutNodeTest {

    @Test
    void testNodeExecutesWithinTimeout() {
        Node<String, String> node = (input) -> "output";
        TimeoutNode<String, String> timeoutNode = new TimeoutNode<>(node, 1, TimeUnit.SECONDS);

        String result = timeoutNode.apply("input");

        assertEquals("output", result);
    }

    @Test
    void testNodeExceedsTimeout() {
        Node<String, String> node = (input) -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "output";
        };
        TimeoutNode<String, String> timeoutNode = new TimeoutNode<>(node, 1, TimeUnit.SECONDS);

        assertThrows(RuntimeException.class, () -> timeoutNode.apply("input"));
    }
}

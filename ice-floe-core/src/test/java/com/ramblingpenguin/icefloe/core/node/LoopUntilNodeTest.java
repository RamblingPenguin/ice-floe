package com.ramblingpenguin.icefloe.core.node;

import com.ramblingpenguin.icefloe.core.Node;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoopUntilNodeTest {

    @Test
    void testLoopExecutesOnce() {
        Node<String, String> node = (input) -> "output";
        Predicate<String> predicate = (output) -> false;
        LoopWhileNode<String, String> loopWhileNode = new LoopWhileNode<>(node, predicate, 3);

        String result = loopWhileNode.apply("input");

        assertEquals("output", result);
    }

    @Test
    void testLoopExecutesMultipleTimes() {
        AtomicInteger attempts = new AtomicInteger(0);
        Node<String, String> node = (input) -> "output" + attempts.getAndIncrement();
        Predicate<String> predicate = (output) -> output.startsWith("output");
        LoopWhileNode<String, String> loopWhileNode = new LoopWhileNode<>(node, predicate, 3);

        String result = loopWhileNode.apply("input");

        assertEquals("output2", result);
    }

    @Test
    void testLoopReachesMaxAttempts() {
        AtomicInteger attempts = new AtomicInteger(0);
        Node<String, String> node = (input) -> "output" + attempts.getAndIncrement();
        Predicate<String> predicate = (output) -> true;
        LoopWhileNode<String, String> loopWhileNode = new LoopWhileNode<>(node, predicate, 3);

        String result = loopWhileNode.apply("input");

        assertEquals("output2", result);
    }
}

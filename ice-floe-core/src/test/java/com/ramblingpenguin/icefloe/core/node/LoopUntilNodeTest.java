package com.ramblingpenguin.icefloe.core.node;

import com.ramblingpenguin.icefloe.core.Node;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoopWhileNodeTest {

    record TestRecord(String value) {}

    @Test
    void testLoopExecutesOnce() {
        Node<TestRecord, TestRecord> node = (input) -> new TestRecord("output");
        Predicate<TestRecord> predicate = (output) -> false;
        LoopWhileNode<TestRecord, TestRecord> LoopWhileNode = new LoopWhileNode<>(node, predicate, 3);

        TestRecord result = LoopWhileNode.apply(new TestRecord("input"));

        assertEquals("output", result.value());
    }

    @Test
    void testLoopExecutesMultipleTimes() {
        AtomicInteger attempts = new AtomicInteger(0);
        Node<TestRecord, TestRecord> node = (input) -> new TestRecord("output" + attempts.getAndIncrement());
        Predicate<TestRecord> predicate = (output) -> output.value().startsWith("output");
        LoopWhileNode<TestRecord, TestRecord> LoopWhileNode = new LoopWhileNode<>(node, predicate, 3);

        TestRecord result = LoopWhileNode.apply(new TestRecord("input"));

        assertEquals("output2", result.value());
    }

    @Test
    void testLoopReachesMaxAttempts() {
        AtomicInteger attempts = new AtomicInteger(0);
        Node<TestRecord, TestRecord> node = (input) -> new TestRecord("output" + attempts.getAndIncrement());
        Predicate<TestRecord> predicate = (output) -> true;
        LoopWhileNode<TestRecord, TestRecord> LoopWhileNode = new LoopWhileNode<>(node, predicate, 3);

        TestRecord result = LoopWhileNode.apply(new TestRecord("input"));

        assertEquals("output2", result.value());
    }
}

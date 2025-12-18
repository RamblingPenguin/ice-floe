package com.ramblingpenguin.icefloe.core.node;

import com.ramblingpenguin.icefloe.core.Node;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SwitchNodeTest {

    record TestRecord(String value) {}

    @Test
    void testSelectsCorrectNode() {
        Node<TestRecord, TestRecord> nodeA = (input) -> new TestRecord("A");
        Node<TestRecord, TestRecord> nodeB = (input) -> new TestRecord("B");
        Node<TestRecord, TestRecord> defaultNode = (input) -> new TestRecord("default");
        Map<String, Node<TestRecord, TestRecord>> nodes = Map.of("A", nodeA, "B", nodeB);
        SwitchNode<TestRecord, TestRecord, String> switchNode = new SwitchNode<>(
                (input) -> input.value(),
                Function.identity(),
                nodes,
                defaultNode
        );

        TestRecord resultA = switchNode.apply(new TestRecord("A"));
        TestRecord resultB = switchNode.apply(new TestRecord("B"));

        assertEquals("A", resultA.value());
        assertEquals("B", resultB.value());
    }

    @Test
    void testSelectsDefaultNode() {
        Node<TestRecord, TestRecord> nodeA = (input) -> new TestRecord("A");
        Node<TestRecord, TestRecord> nodeB = (input) -> new TestRecord("B");
        Node<TestRecord, TestRecord> defaultNode = (input) -> new TestRecord("default");
        Map<String, Node<TestRecord, TestRecord>> nodes = Map.of("A", nodeA, "B", nodeB);
        SwitchNode<TestRecord, TestRecord, String> switchNode = new SwitchNode<>(
                (input) -> input.value(),
                Function.identity(),
                nodes,
                defaultNode
        );

        TestRecord result = switchNode.apply(new TestRecord("C"));

        assertEquals("default", result.value());
    }
}

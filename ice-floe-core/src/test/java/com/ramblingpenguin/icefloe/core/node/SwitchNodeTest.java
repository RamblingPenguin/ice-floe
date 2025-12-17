package com.ramblingpenguin.icefloe.core.node;

import com.ramblingpenguin.icefloe.core.Node;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SwitchNodeTest {

    @Test
    void testSelectsCorrectNode() {
        Node<String, String> nodeA = (input) -> "A";
        Node<String, String> nodeB = (input) -> "B";
        Node<String, String> defaultNode = (input) -> "default";
        Map<String, Node<String, String>> nodes = Map.of("A", nodeA, "B", nodeB);
        SwitchNode<String, String, String> switchNode = new SwitchNode<>(
                (input) -> input,
                Function.identity(),
                nodes,
                defaultNode
        );

        String resultA = switchNode.apply("A");
        String resultB = switchNode.apply("B");

        assertEquals("A", resultA);
        assertEquals("B", resultB);
    }

    @Test
    void testSelectsDefaultNode() {
        Node<String, String> nodeA = (input) -> "A";
        Node<String, String> nodeB = (input) -> "B";
        Node<String, String> defaultNode = (input) -> "default";
        Map<String, Node<String, String>> nodes = Map.of("A", nodeA, "B", nodeB);
        SwitchNode<String, String, String> switchNode = new SwitchNode<>(
                (input) -> input,
                Function.identity(),
                nodes,
                defaultNode
        );

        String result = switchNode.apply("C");

        assertEquals("default", result);
    }
}

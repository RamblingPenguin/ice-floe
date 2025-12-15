package com.ramblingpenguin.icefloe.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ContextualSequenceTest {

    // --- Test Records ---
    public record InitialInput(String message) {}
    public record WordCount(int count) {}
    public record UppercaseMessage(String message) {}

    // --- Test Nodes ---

    public static class WordCounterNode implements ContextualNode<WordCount> {
        private static final NodeKey KEY = NodeKey.of("word-counter");
        @Override public NodeKey getKey() { return KEY; }
        @Override public WordCount apply(SequenceContext context) {
            return new WordCount(context.get(SequenceContext.INITIAL_NODE_KEY, InitialInput.class)
                    .map(i -> i.message().split("\\s+").length).orElse(0));
        }
    }

    public static class UppercaseNode implements ContextualNode<UppercaseMessage> {
        private static final NodeKey KEY = NodeKey.of("uppercase-node");
        @Override public NodeKey getKey() { return KEY; }
        @Override public UppercaseMessage apply(SequenceContext context) {
            return new UppercaseMessage(context.get(SequenceContext.INITIAL_NODE_KEY, InitialInput.class)
                    .map(i -> i.message().toUpperCase()).orElse(""));
        }
    }

    @Test
    public void testContextualSequenceWithNodes() {
        ContextualSequence<InitialInput> sequence = ContextualSequence.Builder.of(InitialInput.class)
                .then(new WordCounterNode())
                .then(new UppercaseNode())
                .build();
        SequenceContext finalContext = sequence.apply(new InitialInput("Hello world"));

        assertEquals(2, finalContext.get(WordCounterNode.KEY, WordCount.class).orElseThrow(IllegalStateException::new).count());
        assertEquals("HELLO WORLD", finalContext.get(UppercaseNode.KEY, UppercaseMessage.class).orElseThrow(IllegalStateException::new).message());
    }

    @Test
    public void testContextualSequenceWithFunctions() {
        NodeKey wordCounterKey = NodeKey.of("word-counter-lambda");
        NodeKey uppercaseKey = NodeKey.of("uppercase-lambda");

        ContextualSequence<InitialInput> sequence = ContextualSequence.Builder.of(InitialInput.class)
                .then(wordCounterKey, context -> new WordCount(context.get(SequenceContext.INITIAL_NODE_KEY, InitialInput.class)
                        .map(i -> i.message().split("\\s+").length).orElse(0)))
                .then(uppercaseKey, context -> new UppercaseMessage(context.get(SequenceContext.INITIAL_NODE_KEY, InitialInput.class)
                        .map(i -> i.message().toUpperCase()).orElse("")))
                .build();

        SequenceContext finalContext = sequence.apply(new InitialInput("Lambda test works"));

        assertEquals(3, finalContext.get(wordCounterKey, WordCount.class).orElseThrow(IllegalStateException::new).count());
        assertEquals("LAMBDA TEST WORKS", finalContext.get(uppercaseKey, UppercaseMessage.class).orElseThrow(IllegalStateException::new).message());
    }

    @Test
    public void testGetNodes() {
        List<ContextualNode<?>> nodes = List.of(new WordCounterNode(), new UppercaseNode());
        ContextualSequence<InitialInput> sequence = ContextualSequence.Builder.of(InitialInput.class)
                .then(nodes.get(0))
                .then(nodes.get(1))
                .build();

        List<ContextualNode<?>> retrievedNodes = sequence.getNodes();
        assertEquals(2, retrievedNodes.size());
        assertEquals(nodes, retrievedNodes);
    }
}

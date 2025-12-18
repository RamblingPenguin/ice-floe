package com.ramblingpenguin.icefloe.core.context;

import com.ramblingpenguin.icefloe.core.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContextualSequenceTest {

    // --- Test Records ---
    public record InitialInput(String message) {}
    public record WordCount(int count) {}
    public record UppercaseMessage(String message) {}

    @Test
    public void testContextualSequenceWithNodes() {
        NodeKey<InitialInput> initialInputKey = new NodeKey<>("initial", InitialInput.class);
        NodeKey<WordCount> wordCounterKey = new NodeKey<>("word-counter", WordCount.class);
        NodeKey<UppercaseMessage> uppercaseKey = new NodeKey<>("uppercase-node", UppercaseMessage.class);

        Node<InitialInput, WordCount> wordCounter = input -> new WordCount(input.message().split("\\s+").length);
        Node<InitialInput, UppercaseMessage> uppercaser = input -> new UppercaseMessage(input.message().toUpperCase());

        ContextualNode<InitialInput, WordCount> wordCounterNode = ContextualNode.of(
                wordCounterKey,
                context -> context.get(initialInputKey).orElseThrow(),
                wordCounter
        );

        ContextualNode<InitialInput, UppercaseMessage> uppercaseNode = ContextualNode.of(
                uppercaseKey,
                context -> context.get(initialInputKey).orElseThrow(),
                uppercaser
        );

        ContextualSequence<InitialInput> sequence = ContextualSequence.Builder.of(InitialInput.class)
                .then(wordCounterNode)
                .then(uppercaseNode)
                .build();

        SequenceContext finalContext = sequence.apply(new InitialInput("Hello world"));

        assertEquals(2, finalContext.get(wordCounterNode.getKey()).orElseThrow().count());
        assertEquals("HELLO WORLD", finalContext.get(uppercaseNode.getKey()).orElseThrow().message());
    }

    @Test
    public void testContextualSequenceWithFunctions() {
        NodeKey<InitialInput> initialInputKey = new NodeKey<>("initial", InitialInput.class);
        NodeKey<WordCount> wordCounterKey = new NodeKey<>("word-counter-lambda", WordCount.class);
        NodeKey<UppercaseMessage> uppercaseKey = new NodeKey<>("uppercase-lambda", UppercaseMessage.class);

        ContextualSequence<InitialInput> sequence = ContextualSequence.Builder.of(InitialInput.class)
                .then(wordCounterKey, context -> new WordCount(context.get(initialInputKey)
                        .map(i -> i.message().split("\\s+").length).orElse(0)))
                .then(uppercaseKey, context -> new UppercaseMessage(context.get(initialInputKey)
                        .map(i -> i.message().toUpperCase()).orElse("")))
                .build();

        SequenceContext finalContext = sequence.apply(new InitialInput("Lambda test works"));

        assertEquals(3, finalContext.get(wordCounterKey).orElseThrow().count());
        assertEquals("LAMBDA TEST WORKS", finalContext.get(uppercaseKey).orElseThrow().message());
    }
}

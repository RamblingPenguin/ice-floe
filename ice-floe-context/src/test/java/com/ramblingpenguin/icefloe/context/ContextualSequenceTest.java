package com.ramblingpenguin.icefloe.context;

import com.ramblingpenguin.icefloe.core.Node;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContextualSequenceTest {

    // --- Test Records ---
    public record InitialInput(String message) implements Serializable {}
    public record WordCount(int count) implements Serializable {}
    public record UppercaseMessage(String message) implements Serializable {}

    @Test
    public void testContextualSequenceWithNodes() {
        NodeKey<InitialInput> initialInputKey = new NodeKey<>("init", InitialInput.class);
        NodeKey<WordCount> wordCounterKey = new NodeKey<>("word-counter", WordCount.class);
        NodeKey<UppercaseMessage> uppercaseKey = new NodeKey<>("uppercase-node", UppercaseMessage.class);

        Node<InitialInput, WordCount> wordCounter = input -> new WordCount(input.message().split("\\s+").length);
        Node<InitialInput, UppercaseMessage> uppercaser = input -> new UppercaseMessage(input.message().toUpperCase());

        ContextualSequence<InitialInput> sequence = ContextualSequence.Builder.of(initialInputKey)
                .then(initialInputKey, wordCounterKey, wordCounter)
                .then(initialInputKey, uppercaseKey, uppercaser)
                .build();

        SequenceContext finalContext = sequence.apply(new InitialInput("Hello world"));

        assertEquals(2, finalContext.get(wordCounterKey).orElseThrow().count());
        assertEquals("HELLO WORLD", finalContext.get(uppercaseKey).orElseThrow().message());
    }

    @Test
    public void testContextualSequenceWithFunctions() {
        NodeKey<InitialInput> initialInputKey = new NodeKey<>("init", InitialInput.class);
        NodeKey<WordCount> wordCounterKey = new NodeKey<>("word-counter-lambda", WordCount.class);
        NodeKey<UppercaseMessage> uppercaseKey = new NodeKey<>("uppercase-lambda", UppercaseMessage.class);

        ContextualSequence<InitialInput> sequence = ContextualSequence.Builder.of(initialInputKey)
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

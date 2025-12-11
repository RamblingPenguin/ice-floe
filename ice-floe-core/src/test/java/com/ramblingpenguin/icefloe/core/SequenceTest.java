package com.ramblingpenguin.icefloe.core;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SequenceTest {

    public static class ParseIntNode implements Node<String, Integer> {
        @Override
        public Integer apply(String s) {
            return Integer.parseInt(s);
        }
    }

    public static class DoubleNode implements Node<Integer, Integer> {
        @Override
        public Integer apply(Integer i) {
            return i * 2;
        }
    }

    @Test
    public void testBuildSequence() {
        Sequence<String, Map<String, Integer>> sequence = Sequence.Builder.of(String.class)
                .then(new ParseIntNode())
                .then(new DoubleNode())
                .then(new PredicateNode<Integer, Map<String, Integer>>(i -> i > 200, i -> Map.of("value", i), i -> Map.of()))
                .build();

        assertEquals(246, sequence.apply("123").get("value"));
    }

    @Test
    public void testBuildSequenceWithLambdas() {
        Sequence<String, Integer> sequence = Sequence.Builder.of(String.class)
                .then(Integer::parseInt)
                .then(i -> i * 2)
                .build();

        assertEquals(246, sequence.apply("123"));
    }
}

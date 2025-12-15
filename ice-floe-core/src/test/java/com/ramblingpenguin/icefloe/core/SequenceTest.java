package com.ramblingpenguin.icefloe.core;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

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
    public void testBuildSequenceWithNodes() {
        Sequence<String, Integer> sequence = Sequence.Builder.of(String.class)
                .then(new ParseIntNode())
                .then(new DoubleNode())
                .build();

        assertEquals(246, sequence.apply("123"));
    }

    @Test
    public void testBuildSequenceWithFunctions() {
        Sequence<String, String> sequence = Sequence.Builder.of(String.class)
                .then(Integer::parseInt)
                .then(i -> i * 2)
                .then((Function<Integer, String>) Object::toString)
                .build();

        assertEquals("246", sequence.apply("123"));
    }


}

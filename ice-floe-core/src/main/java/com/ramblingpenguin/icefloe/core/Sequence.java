package com.ramblingpenguin.icefloe.core;

import java.util.function.Function;

/**
 * A type-safe sequence of nodes that are composed together to form a single, executable node.
 * The composition ensures that the output of one node is compatible with the input of the next at compile time.
 *
 * @param <INPUT>  The input type of the entire sequence.
 * @param <OUTPUT> The output type of the entire sequence.
 */
public class Sequence<INPUT, OUTPUT> implements Node<INPUT, OUTPUT> {

    private final Node<INPUT, OUTPUT> composedNode;

    protected Sequence(Node<INPUT, OUTPUT> composedNode) {
        this.composedNode = composedNode;
    }

    @Override
    public OUTPUT apply(INPUT input) {
        return composedNode.apply(input);
    }

    /**
     * A private internal node that safely composes two nodes together.
     * It takes a node from A to B and a node from B to C and creates a single node from A to C.
     */
    private static class Pair<A, B, C> implements Node<A, C> {
        private final Node<A, B> first;
        private final Node<B, C> second;

        private Pair(Node<A, B> first, Node<B, C> second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public C apply(A a) {
            return second.apply(first.apply(a));
        }
    }

    /**
     * A fluent builder that ensures end-to-end type safety for a sequence of nodes.
     *
     * @param <SEQUENCE_INPUT> The initial input type of the sequence.
     * @param <CURRENT_OUTPUT> The output type of the last node added to the chain.
     */
    public static class Builder<SEQUENCE_INPUT, CURRENT_OUTPUT> {

        private final Node<SEQUENCE_INPUT, CURRENT_OUTPUT> composedNode;

        private Builder(Node<SEQUENCE_INPUT, CURRENT_OUTPUT> composedNode) {
            this.composedNode = composedNode;
        }

        /**
         * Starts a new sequence builder.
         *
         * @param inputType The class of the initial input.
         * @param <T>       The type of the initial input.
         * @return A new builder instance.
         */
        public static <T> Builder<T, T> of(@SuppressWarnings("unused") Class<T> inputType) {
            // The initial node is an identity function.
            return new Builder<>(input -> input);
        }

        /**
         * Appends a new node to the sequence.
         *
         * @param nextNode The next node to add to the chain. Its input type must match the
         *                 current output type of the builder.
         * @param <NEXT_OUTPUT> The output type of the new node.
         * @return A new builder instance with the updated composition.
         */
        public <NEXT_OUTPUT> Builder<SEQUENCE_INPUT, NEXT_OUTPUT> then(Node<CURRENT_OUTPUT, NEXT_OUTPUT> nextNode) {
            return new Builder<>(new Pair<>(this.composedNode, nextNode));
        }

        /**
         * A convenience method to append a node from a lambda function.
         *
         * @param func The function to add as the next node in the chain.
         * @param <NEXT_OUTPUT> The output type of the function.
         * @return A new builder instance with the updated composition.
         */
        public <NEXT_OUTPUT> Builder<SEQUENCE_INPUT, NEXT_OUTPUT> then(Function<CURRENT_OUTPUT, NEXT_OUTPUT> func) {
            return then(func::apply);
        }

        /**
         * Builds the final, executable {@link Sequence}.
         *
         * @return A new, immutable, and type-safe Sequence.
         */
        public Sequence<SEQUENCE_INPUT, CURRENT_OUTPUT> build() {
            return new Sequence<>(composedNode);
        }
    }
}

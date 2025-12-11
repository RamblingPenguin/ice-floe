package com.ramblingpenguin.icefloe.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Represents a linear sequence of {@link Node}s executed in order.
 * <p>
 * The output of one node becomes the input of the next. Use the inner {@link Builder}
 * class to construct a {@code Sequence} with compile-time type safety.
 *
 * @param <INPUT>  The input type of the first node in the sequence.
 * @param <OUTPUT> The output type of the last node in the sequence.
 */
public class Sequence<INPUT, OUTPUT> implements Node<INPUT, OUTPUT> {

    private final List<Node<?, ?>> nodes;

    private Sequence(List<Node<?, ?>> nodes) {
        this.nodes = new ArrayList<>(nodes);
    }

    /**
     * Executes the sequence of nodes.
     *
     * @param input the initial input to the sequence.
     * @return the final output after passing through all nodes.
     */
    @Override
    @SuppressWarnings("unchecked")
    public OUTPUT apply(INPUT input) {
        Object result = input;
        for (Node<?, ?> node : nodes) {
            result = ((Node<Object, ?>)node).apply(result);
        }
        return (OUTPUT) result;
    }

    @Override
    public void accept(IceFloeVisitor visitor) {
        visitor.visitSequence(this);
    }

    public List<Node<?, ?>> getNodes() {
        return java.util.Collections.unmodifiableList(nodes);
    }

    /**
     * A fluent builder for creating {@link Sequence} instances.
     * <p>
     * This builder enforces type safety by ensuring that the output type of the current step
     * matches the input type of the added next step.
     *
     * @param <SEQUENCE_INPUT> The input type of the entire sequence being built.
     * @param <CURRENT_OUTPUT> The output type of the last node currently added to the builder.
     */
    public static class Builder<SEQUENCE_INPUT, CURRENT_OUTPUT> {

        private final List<Node<?, ?>> nodes;

        private Builder(List<Node<?, ?>> nodes) {
            this.nodes = nodes;
        }

        /**
         * Starts a new sequence builder for the specified input type.
         *
         * @param inputType The class of the input type.
         * @param <T>       The input type.
         * @return A new Builder instance.
         */
        public static <T> Builder<T, T> of(Class<T> inputType) {
            return new Builder<>(new LinkedList<>());
        }

        /**
         * Appends a {@link Node} to the sequence.
         *
         * @param node          The node to append.
         * @param <NEXT_OUTPUT> The output type of the new node.
         * @return A new Builder instance parameterized with the new output type.
         */
        public <NEXT_OUTPUT> Builder<SEQUENCE_INPUT, NEXT_OUTPUT> then(Node<CURRENT_OUTPUT, NEXT_OUTPUT> node) {
            List<Node<?, ?>> newNodes = new ArrayList<>(this.nodes);
            newNodes.add(node);
            return new Builder<>(newNodes);
        }

        /**
         * Appends a standard Java {@link Function} to the sequence, wrapping it as a {@link Node}.
         *
         * @param func          The function to append.
         * @param <NEXT_OUTPUT> The output type of the function.
         * @return A new Builder instance parameterized with the new output type.
         */
        public <NEXT_OUTPUT> Builder<SEQUENCE_INPUT, NEXT_OUTPUT> then(Function<CURRENT_OUTPUT, NEXT_OUTPUT> func) {
            return then((Node<CURRENT_OUTPUT, NEXT_OUTPUT>) func::apply);
        }

        /**
         * Builds the final {@link Sequence}.
         *
         * @return The constructed Sequence.
         */
        public Sequence<SEQUENCE_INPUT, CURRENT_OUTPUT> build() {
            return new Sequence<>(nodes);
        }
    }
}
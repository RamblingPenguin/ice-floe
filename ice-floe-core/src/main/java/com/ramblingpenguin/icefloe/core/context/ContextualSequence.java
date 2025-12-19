package com.ramblingpenguin.icefloe.core.context;

import com.ramblingpenguin.icefloe.core.Node;
import com.ramblingpenguin.icefloe.core.Sequence;

import java.io.Serializable;

/**
 * A type-safe, stateful sequence of nodes that pass an immutable {@link SequenceContext}
 * through a compositional chain. Each node receives the context from the previous step
 * and returns a new, updated context for the next, ensuring thread safety.
 *
 * @param <INPUT> The type of the initial input that starts the sequence.
 */
public class ContextualSequence<INPUT> extends Sequence<INPUT, SequenceContext> {

    private ContextualSequence(Node<INPUT, SequenceContext> composedNode) {
        super(composedNode);
    }

    /**
     * A fluent builder for creating {@link ContextualSequence} instances with end-to-end type safety.
     *
     * @param <SEQUENCE_INPUT> The initial input type for the sequence.
     */
    public static class Builder<SEQUENCE_INPUT> {

        private final Sequence.Builder<SEQUENCE_INPUT, SequenceContext> sequenceBuilder;

        private Builder(Sequence.Builder<SEQUENCE_INPUT, SequenceContext> sequenceBuilder) {
            this.sequenceBuilder = sequenceBuilder;
        }

        /**
         * Starts a new contextual sequence builder.
         * The builder is initialized to take an input of the specified type and convert it
         * into the starting {@link SequenceContext}.
         *
         * @param inputType The class of the initial input.
         * @param <T>       The type of the initial input.
         * @return A new builder instance.
         */
        public static <T extends Serializable> Builder<T> of(Class<T> inputType) {
            Sequence.Builder<T, SequenceContext> initialBuilder = Sequence.Builder
                    .of(inputType)
                    .then(SequenceContext::fromInitialInput);
            return new Builder<>(initialBuilder);
        }

        /**
         * Appends a node that operates on and returns a {@link SequenceContext}.
         * This is the most general way to add a step to the sequence, and it is the required
         * method for adding complex nodes like {@link ContextualForkSequence}.
         *
         * @param nextNode The node to append.
         * @return A new builder instance with the node added.
         */
        public Builder<SEQUENCE_INPUT> then(Node<SequenceContext, SequenceContext> nextNode) {
            return new Builder<>(this.sequenceBuilder.then(nextNode));
        }

        /**
         * Appends a pre-configured {@link ContextualNode} to the sequence.
         * This is a convenience method that delegates to the more general {@code then(Node)} method.
         *
         * @param node The contextual node to add.
         * @return This builder instance for chaining.
         */
        public Builder<SEQUENCE_INPUT> then(ContextualNode<?, ?> node) {
            return then((Node<SequenceContext, SequenceContext>) node);
        }

        /**
         * A convenience method to add a simple value-producing node to the sequence.
         * This wraps the provided function in a {@link ContextualNode} that takes the
         * full context as input and stores the result under the given key.
         *
         * @param nodeKey The key to store the node's output under.
         * @param func    The node logic, which takes the full context and returns an output.
         * @param <OUTPUT> The output type of the node.
         * @return This builder instance for chaining.
         */
        public <OUTPUT extends Serializable> Builder<SEQUENCE_INPUT> then(NodeKey<OUTPUT> nodeKey, Node<SequenceContext, OUTPUT> func) {
            ContextualNode<SequenceContext, OUTPUT> node = ContextualNode.of(nodeKey, ctx -> ctx, func);
            return then(node);
        }

        /**
         * Builds the final, executable {@link ContextualSequence}.
         *
         * @return A new, immutable, and type-safe ContextualSequence.
         */
        public ContextualSequence<SEQUENCE_INPUT> build() {
            return new ContextualSequence<>(sequenceBuilder.build());
        }
    }
}

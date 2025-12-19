package com.ramblingpenguin.icefloe.context;

import com.ramblingpenguin.glacier.trait.Identifiable;
import com.ramblingpenguin.icefloe.core.Node;
import com.ramblingpenguin.icefloe.core.Sequence;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A type-safe, stateful sequence of nodes that pass an immutable {@link SequenceContext}
 * through a compositional chain. This class is now a pure context-in, context-out pipeline.
 * The creation of the initial context is handled by the execution environment (e.g., SequenceService).
 */
public class ContextualSequence<INPUT> extends Sequence<INPUT, SequenceContext> implements Identifiable<SequenceKey<INPUT, SequenceContext>> {

    private final SequenceKey<INPUT, SequenceContext> sequenceKey;

    private ContextualSequence(SequenceKey<INPUT, SequenceContext> sequenceKey, Node<INPUT, SequenceContext> composedNode) {
        super(composedNode);
        this.sequenceKey = Objects.requireNonNull(sequenceKey);
    }

    public SequenceKey<INPUT, SequenceContext> id() {
        return sequenceKey;
    }

    /**
     * A fluent builder for creating {@link ContextualSequence} instances.
     */
    public static class Builder<INPUT extends Serializable> {

        private final NodeKey<INPUT> inputNodeKey;
        private final Sequence.Builder<INPUT, SequenceContext> sequenceBuilder;

        private Builder(NodeKey<INPUT> inputNodeKey, Sequence.Builder<INPUT, SequenceContext> sequenceBuilder) {
            this.inputNodeKey = inputNodeKey;
            this.sequenceBuilder = sequenceBuilder;
        }

        /**
         * Starts a new contextual sequence builder.
         * @return A new builder instance.
         */
        public static <INPUT extends Serializable> Builder<INPUT> of(NodeKey<INPUT> inputNodeKey, SequenceStateService recorder) {
            return new Builder<>(inputNodeKey, Sequence.Builder.of(inputNodeKey.outputType(), input -> {
                SequenceContext context = SequenceContext.newRootContext(inputNodeKey, input, new DefaultTypeCombinerFactory());
                if (recorder != null) {
                    recorder.beginExecution(context);
                }
                return context;
            }));
        }

        /**
         * Starts a new contextual sequence builder.
         * @return A new builder instance.
         */
        public static <INPUT extends Serializable> Builder<INPUT> of(NodeKey<INPUT> inputNodeKey) {
            return of(inputNodeKey, null);
        }

        /**
         * Appends a node that operates on and returns a {@link SequenceContext}.
         */
        public Builder<INPUT> then(Node<SequenceContext, SequenceContext> nextNode) {
            return new Builder<>(this.inputNodeKey, this.sequenceBuilder.then(nextNode));
        }

        /**
         * A convenience method to add a simple value-producing node to the sequence.
         * This wraps the provided function in a {@link ContextualNode} that takes the
         * full context as input and stores the result under the given key.
         */
        public <OUTPUT extends Serializable> Builder<INPUT> then(NodeKey<OUTPUT> nodeKey, Node<SequenceContext, OUTPUT> func) {
            ContextualNode<SequenceContext, OUTPUT> node = ContextualNode.of(nodeKey, ctx -> ctx, func);
            return then(node);
        }

        /**
         * Appends a pre-configured {@link ContextualNode} to the sequence.
         */
        public Builder<INPUT> then(ContextualNode<?, ?> node) {
            return then((Node<SequenceContext, SequenceContext>) node);
        }

        /**
         * Appends a "transformer" node that takes one input from the context, processes it,
         * and stores the output back into the context.
         */
        public <IN, OUT extends Serializable> Builder<INPUT> then(
                NodeKey<IN> inputKey,
                NodeKey<OUT> outputKey,
                Node<IN, OUT> node) {
            ContextualNode<IN, OUT> contextualNode = ContextualNode.of(
                    outputKey,
                    ctx -> ctx.get(inputKey).orElseThrow(),
                    node
            );
            return then(contextualNode);
        }

        /**
         * Appends a "producer" node that takes no input from the context and produces an output.
         */
        public <OUT extends Serializable> Builder<INPUT> thenProduce(
                NodeKey<OUT> outputKey,
                Supplier<OUT> supplier) {
            ContextualNode<Void, OUT> contextualNode = ContextualNode.of(
                    outputKey,
                    ctx -> null,
                    input -> supplier.get()
            );
            return then(contextualNode);
        }

        /**
         * Builds the final, executable {@link ContextualSequence}.
         */
        public ContextualSequence<INPUT> build() {
            return new ContextualSequence<>(SequenceKey.newUUID(inputNodeKey.outputType(), SequenceContext.class), sequenceBuilder.build());
        }
    }
}

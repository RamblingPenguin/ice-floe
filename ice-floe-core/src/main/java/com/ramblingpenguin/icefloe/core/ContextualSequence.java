package com.ramblingpenguin.icefloe.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * A sequence that passes a {@link SequenceContext} to each node.
 * Each node's output is added to the context, making it available to subsequent nodes.
 */
public class ContextualSequence<INPUT extends Record> extends Sequence<INPUT, SequenceContext> {

    private ContextualSequence(List<ContextualNode<?>> nodes) {
        super(nodes);
    }

    @Override
    public SequenceContext apply(INPUT initialContext) {
        SequenceContext context = SequenceContext.fromInitialInput(initialContext);
        for (Node<?, ?> node : super.getNodes()) {
            if (node instanceof ContextualNode<?> contextualNode) {
                Record output = contextualNode.apply(context);
                context.put(contextualNode.getKey(), output);
            } else {
                throw new IllegalStateException("Node must implement ContextualNode");
            }
        }
        return context;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ContextualNode<?>> getNodes() {
        return (List<ContextualNode<?>>) super.getNodes();
    }

    public static class Builder<SEQUENCE_INPUT extends Record> {

        private final List<ContextualNode<?>> nodes;

        private Builder(List<ContextualNode<?>> nodes) {
            this.nodes = nodes;
        }

        public static <T extends Record> Builder<T> of(@SuppressWarnings("unused") Class<T> inputType) {
            return new Builder<>(new LinkedList<>());
        }

        public Builder<SEQUENCE_INPUT> then(ContextualNode<? extends Record> node) {
            List<ContextualNode<?>> newNodes = new ArrayList<>(this.nodes);
            newNodes.add(node);
            return new Builder<>(newNodes);
        }

        public Builder<SEQUENCE_INPUT> then(NodeKey nodeKey, Function<SequenceContext, ? extends Record> func) {
            return then(ContextualNode.of(nodeKey, func));
        }

        public ContextualSequence<SEQUENCE_INPUT> build() {
            return new ContextualSequence<>(nodes);
        }
    }
}

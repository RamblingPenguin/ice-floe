package com.ramblingpenguin.icefloe.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class Sequence<INPUT, OUTPUT> implements Node<INPUT, OUTPUT> {

    protected final List<? extends Node<?, ?>> nodes;

    protected Sequence(List<? extends Node<?, ?>> nodes) {
        this.nodes = new ArrayList<>(nodes);
    }

    public List<? extends Node<?, ?>> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    @Override
    public void accept(IceFloeVisitor visitor) {
        visitor.visitSequence(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public OUTPUT apply(INPUT input) {
        Object result = input;
        for (Node<?, ?> node : nodes) {
            result = ((Node<Object, ?>)node).apply(result);
        }
        return (OUTPUT) result;
    }

    public static class Builder<SEQUENCE_INPUT, CURRENT_OUTPUT> {

        private final List<Node<?, ?>> nodes;

        private Builder(List<Node<?, ?>> nodes) {
            this.nodes = nodes;
        }

        public static <T> Builder<T, T> of(@SuppressWarnings("unused") Class<T> inputType) {
            return new Builder<>(new LinkedList<>());
        }

        public <NEXT_OUTPUT> Builder<SEQUENCE_INPUT, NEXT_OUTPUT> then(Node<CURRENT_OUTPUT, NEXT_OUTPUT> node) {
            List<Node<?, ?>> newNodes = new ArrayList<>(this.nodes);
            newNodes.add(node);
            return new Builder<>(newNodes);
        }

        public <NEXT_OUTPUT> Builder<SEQUENCE_INPUT, NEXT_OUTPUT> then(Function<CURRENT_OUTPUT, NEXT_OUTPUT> func) {
            return then(func::apply);
        }

        public Sequence<SEQUENCE_INPUT, CURRENT_OUTPUT> build() {
            return new Sequence<>(nodes);
        }
    }
}

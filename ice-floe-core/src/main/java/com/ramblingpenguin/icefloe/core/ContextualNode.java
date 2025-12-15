package com.ramblingpenguin.icefloe.core;

import java.util.function.Function;

/**
 * A node that operates within a {@link ContextualSequence}.
 * It receives the entire {@link SequenceContext} as input and is expected to return a result.
 * @param <OUTPUT> The type of the output of this node.
 */
public interface ContextualNode<OUTPUT extends Record> extends Node<SequenceContext, OUTPUT> {

    /**
     * Gets the unique key that identifies this node.
     * This key is used to store and retrieve the node's output from the {@link SequenceContext}.
     * @return The unique key for this node.
     */
    NodeKey getKey();

    /**
     * Creates a ContextualNode from a key and a lambda function.
     *
     * @param key The key for the node.
     * @param function The function that implements the node's logic.
     * @param <O> The output type of the node.
     * @return A new ContextualNode instance.
     */
    static <O extends Record> ContextualNode<O> of(NodeKey key, Function<SequenceContext, O> function) {
        return new ContextualNode<>() {
            @Override
            public NodeKey getKey() {
                return key;
            }

            @Override
            public O apply(SequenceContext context) {
                return function.apply(context);
            }
        };
    }
}

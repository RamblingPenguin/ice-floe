package com.ramblingpenguin.icefloe.core.node;

import com.ramblingpenguin.icefloe.core.Node;

import java.util.function.Predicate;

/**
 * A node that executes a primary node and, if it fails, executes a fallback node.
 *
 * @param <INPUT>  the input type
 * @param <OUTPUT> the output type
 */
public class FallbackNode<INPUT, OUTPUT> implements Node<INPUT, OUTPUT> {

    private final Node<INPUT, OUTPUT> primaryNode;
    private final Node<INPUT, OUTPUT> fallbackNode;

    /**
     * Constructs a new fallback node.
     *
     * @param primaryNode  the primary node to execute
     * @param fallbackNode the fallback node to execute if the primary node fails
     */
    public FallbackNode(Node<INPUT, OUTPUT> primaryNode, Node<INPUT, OUTPUT> fallbackNode) {
        this.primaryNode = primaryNode;
        this.fallbackNode = fallbackNode;
    }

    @Override
    public OUTPUT apply(INPUT input) {
        try {
            return this.primaryNode.apply(input);
        } catch (Throwable throwable) {
            if (shouldUseFallback().test(throwable)) {
                return this.fallbackNode.apply(input);
            } else {
                throw throwable;
            }
        }
    }

    /**
     * Determines whether the fallback node should be used.
     *
     * @return a predicate that returns true if the fallback node should be used, false otherwise
     */
    public Predicate<Throwable> shouldUseFallback() {
        return (t) -> true;
    }
}

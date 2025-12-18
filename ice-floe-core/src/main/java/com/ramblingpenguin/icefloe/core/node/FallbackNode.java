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
    private final Predicate<Throwable> shouldUseFallback;

    /**
     * Constructs a new fallback node with a predicate to determine if the fallback should be used.
     *
     * @param primaryNode       the primary node to execute
     * @param fallbackNode      the fallback node to execute if the primary node fails
     * @param shouldUseFallback a predicate to determine if the fallback should be used
     */
    public FallbackNode(Node<INPUT, OUTPUT> primaryNode, Node<INPUT, OUTPUT> fallbackNode, Predicate<Throwable> shouldUseFallback) {
        this.primaryNode = primaryNode;
        this.fallbackNode = fallbackNode;
        this.shouldUseFallback = shouldUseFallback;
    }

    /**
     * Constructs a new fallback node that always uses the fallback on failure.
     *
     * @param primaryNode  the primary node to execute
     * @param fallbackNode the fallback node to execute if the primary node fails
     */
    public FallbackNode(Node<INPUT, OUTPUT> primaryNode, Node<INPUT, OUTPUT> fallbackNode) {
        this(primaryNode, fallbackNode, (t) -> true);
    }

    @Override
    public OUTPUT apply(INPUT input) {
        try {
            return this.primaryNode.apply(input);
        } catch (Throwable throwable) {
            if (this.shouldUseFallback.test(throwable)) {
                return this.fallbackNode.apply(input);
            } else {
                throw throwable;
            }
        }
    }
}

package com.ramblingpenguin.icefloe.core.node;

import com.ramblingpenguin.icefloe.core.Node;

/**
 * Abstract base class for nodes that loop over a node.
 *
 * @param <INPUT>  the input type
 * @param <OUTPUT> the output type
 */
public abstract class AbstractLoopingNode<INPUT, OUTPUT> implements Node<INPUT, OUTPUT> {

    protected final int maximumIterations;
    protected final Node<INPUT, OUTPUT> toExecute;

    /**
     * Constructs a new looping node.
     *
     * @param toExecute         the node to execute in the loop
     * @param maximumIterations the maximum number of times to loop
     */
    public AbstractLoopingNode(Node<INPUT, OUTPUT> toExecute, int maximumIterations) {
        this.toExecute = toExecute;
        this.maximumIterations = maximumIterations;
    }
}

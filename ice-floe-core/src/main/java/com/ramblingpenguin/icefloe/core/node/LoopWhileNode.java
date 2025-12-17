package com.ramblingpenguin.icefloe.core.node;

import com.ramblingpenguin.icefloe.core.Node;

import java.util.function.Predicate;

/**
 * A node that loops over a node until a predicate is met or a maximum number of attempts is reached.
 *
 * @param <INPUT>  the input type
 * @param <OUTPUT> the output type
 */
public class LoopWhileNode<INPUT, OUTPUT> implements Node<INPUT, OUTPUT> {

    private final Node<INPUT, OUTPUT> node;
    private final Predicate<OUTPUT> retryPredicate;
    private final int maxAttempts;

    /**
     * Constructs a new loop until node.
     *
     * @param node           the node to execute in the loop
     * @param retryPredicate the predicate to test the output against
     * @param maxAttempts    the maximum number of times to loop
     */
    public LoopWhileNode(Node<INPUT, OUTPUT> node, Predicate<OUTPUT> retryPredicate, int maxAttempts) {
        this.node = node;
        this.retryPredicate = retryPredicate;
        this.maxAttempts = maxAttempts;
    }

    @Override
    public OUTPUT apply(INPUT input) {
        OUTPUT output;
        int attempt = 0;
        do {
            output = this.node.apply(input);
        } while (retryPredicate.test(output) && ++attempt < this.maxAttempts);
        return  output;
    }
}

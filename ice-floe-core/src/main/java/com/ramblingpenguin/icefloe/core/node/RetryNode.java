package com.ramblingpenguin.icefloe.core.node;

import com.ramblingpenguin.icefloe.core.Node;

import java.util.function.Predicate;

/**
 * A node that retries a node a maximum number of times.
 *
 * @param <INPUT>  the input type
 * @param <OUTPUT> the output type
 */
public class RetryNode<INPUT, OUTPUT> extends AbstractLoopingNode<INPUT, OUTPUT> {

    /**
     * Constructs a new retry node.
     *
     * @param toExecute         the node to execute
     * @param maximumIterations the maximum number of times to retry
     */
    public RetryNode(Node<INPUT, OUTPUT> toExecute, int maximumIterations) {
        super(toExecute, maximumIterations);
    }

    @Override
    public OUTPUT apply(INPUT input) {
        int attempt = 0;
        Throwable lastException = null;
        while (attempt++ < this.maximumIterations) {
            try {
                return this.toExecute.apply(input);
            } catch (Throwable throwable) {
                if (!this.shouldRetry().test(throwable)) {
                    throw new RuntimeException("Unexpected exceptional case in retry. " + throwable.getMessage(), throwable);
                } else {
                    lastException = throwable;
                }
            }
        }
        if (lastException != null) {
            throw new RuntimeException(String.format("Failed to execute successfully after %d attempts. Due to: %s", attempt, lastException.getMessage()), lastException);
        } else {
            throw new RuntimeException(String.format("Failed to execute successfully after %d attempts.", attempt));
        }

    }

    /**
     * Determines whether the node should be retried.
     *
     * @return a predicate that returns true if the node should be retried, false otherwise
     */
    public Predicate<Throwable> shouldRetry() {
        return (e) -> true;
    }
}

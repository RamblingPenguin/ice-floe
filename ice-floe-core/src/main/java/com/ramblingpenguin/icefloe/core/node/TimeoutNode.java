package com.ramblingpenguin.icefloe.core.node;

import com.ramblingpenguin.icefloe.core.Node;

import java.util.concurrent.*;

/**
 * A node that executes a node with a timeout.
 *
 * @param <INPUT>  the input type
 * @param <OUTPUT> the output type
 */
public class TimeoutNode<INPUT, OUTPUT> implements Node<INPUT, OUTPUT> {

    private final long timeout;
    private final TimeUnit timeUnit;
    private final Node<INPUT, OUTPUT> node;

    /**
     * Constructs a new timeout node.
     *
     * @param node     the node to execute
     * @param timeout  the timeout value
     * @param timeUnit the time unit of the timeout
     */
    public TimeoutNode(Node<INPUT, OUTPUT> node, long timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.node = node;
        this.timeUnit = timeUnit;
    }

    @Override
    public OUTPUT apply(INPUT input) {
        try (ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor()) {
            CompletableFuture<OUTPUT> future = new CompletableFuture<>();
            singleThreadExecutor.execute(() -> {
                future.complete(this.node.apply(input));
            });
            CompletableFuture<OUTPUT> completedFuture = future.orTimeout(this.timeout, this.timeUnit);
            if (completedFuture.isCompletedExceptionally()) {
                throw new RuntimeException(new TimeoutException("Node failed to execute in time limit"));
            }
            try {
                return completedFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

package com.ramblingpenguin.icefloe.core.node;

import com.ramblingpenguin.glacier.observe.Listener;
import com.ramblingpenguin.glacier.observe.Observable;
import com.ramblingpenguin.icefloe.core.Node;

import java.util.concurrent.*;

/**
 * A node that waits for an external event to occur, designed to be stateless and thread-safe.
 * It uses a Phaser to correctly and safely synchronize the waiting thread and the event listener.
 *
 * @param <INPUT>  The input type (often ignored, but required by the Node interface).
 * @param <EVENT>  The type of the event to wait for.
 */
public class EventWaitNode<INPUT, EVENT> implements Node<INPUT, EVENT> {

    private final long timeout;
    private final TimeUnit timeUnit;
    private final Observable<Listener<EVENT>> observable;

    public EventWaitNode(long timeout, TimeUnit timeUnit, Observable<Listener<EVENT>> observable) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.observable = observable;
    }

    @Override
    public EVENT apply(INPUT input) {
        final CompletableFuture<EVENT> future = new CompletableFuture<>();
        // Phaser starts with one party (this thread).
        final Phaser phaser = new Phaser(1);

        Listener<EVENT> listener = event -> {
            // This check prevents completing the future if the node has already timed out.
            if (!future.isDone()) {
                future.complete(event);
            }
            // The listener's job is done; it arrives and deregisters from the phaser.
            // This will unblock the main thread if it's waiting.
            phaser.arriveAndDeregister();
        };

        // Register a new party for the listener, bringing the total to 2.
        phaser.register();
        observable.addListener(listener);

        try {
            // Arrive and wait for the event to arrive (or for timeout).
            // This creates a rendezvous point. If the event thread arrives here first,
            // it will wait for this thread. If this thread arrives first, it will wait for the event.
            phaser.awaitAdvanceInterruptibly(phaser.arrive(), this.timeout, this.timeUnit);
            if (future.isDone()) {
                return future.get();
            } else {
                throw new TimeoutException("Timed out waiting for event.");
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            future.cancel(true); // Ensure the future is cancelled on failure.
            throw new RuntimeException("Failed while waiting for event.", e);
        } finally {
            // Crucially, always remove the listener to prevent memory leaks.
            observable.removeListener(listener);
        }
    }
}

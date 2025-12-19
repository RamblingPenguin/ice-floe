package com.ramblingpenguin.icefloe.core.node;

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

    public abstract static class SimpleSubscriber<EVENT> implements Flow.Subscriber<EVENT> {

        private Flow.Subscription subscription;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            this.subscription = subscription;
            subscription.request(1);
        }

        @Override
        public void onError(Throwable throwable) {

        }

        @Override
        public void onComplete() {
            if (subscription != null) {
                subscription.cancel();
            }
        }
    }

    private final long timeout;
    private final TimeUnit timeUnit;
    private final Flow.Publisher<EVENT> eventPublisher;

    public EventWaitNode(long timeout, TimeUnit timeUnit, Flow.Publisher<EVENT> eventPublisher) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public EVENT apply(INPUT input) {
        final CompletableFuture<EVENT> future = new CompletableFuture<>();
        // Phaser starts with one party (this thread).
        final Phaser phaser = new Phaser(1);

        Flow.Subscriber<EVENT> listener = new SimpleSubscriber<>() {
            @Override
            public void onNext(EVENT event) {
                if (!future.isDone()) {
                    future.complete(event);
                }
                // The listener's job is done; it arrives and deregisters from the phaser.
                // This will unblock the main thread if it's waiting.
                phaser.arriveAndDeregister();
            }
        };
        // Register a new party for the listener, bringing the total to 2.
        phaser.register();
        eventPublisher.subscribe(listener);

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
            listener.onComplete();
        }
    }
}

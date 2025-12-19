package com.ramblingpenguin.icefloe.core.node;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class EventWaitNodeTest {

    record TestRecord(String value) {}

    @Test
    void testEventReceived() throws InterruptedException {
        Flow.Publisher<TestRecord> publisher = mock(Flow.Publisher.class);

        EventWaitNode<TestRecord, TestRecord> node = new EventWaitNode<>(1, TimeUnit.SECONDS, publisher);

        // Use ArgumentCaptor to grab the listener when it's added
        ArgumentCaptor<Flow.Subscriber<TestRecord>> listenerCaptor = ArgumentCaptor.forClass(Flow.Subscriber.class);

        Flow.Subscription subscription = mock(Flow.Subscription.class);

        // Configure the publisher to call onSubscribe when subscribe is called
        doAnswer(invocation -> {
            Flow.Subscriber<TestRecord> subscriber = invocation.getArgument(0);
            subscriber.onSubscribe(subscription);
            return null;
        }).when(publisher).subscribe(any());

        // Start the node execution in a separate thread so we can simulate an external event
        Thread executionThread = new Thread(() -> {
            TestRecord result = node.apply(new TestRecord("input"));
            assertEquals("event-data", result.value());
        });
        executionThread.start();

        // Give the execution thread a moment to add the listener
        Thread.sleep(100);

        // Verify the listener was added and capture it
        verify(publisher).subscribe(listenerCaptor.capture());
        Flow.Subscriber<TestRecord> capturedListener = listenerCaptor.getValue();

        // Simulate the event firing
        capturedListener.onNext(new TestRecord("event-data"));

        // Wait for the execution thread to finish
        executionThread.join();

        // Verify the listener was removed (subscription cancelled)
        verify(subscription).cancel();
    }

    @Test
    void testTimeout() {
        Flow.Publisher<TestRecord> publisher = mock(Flow.Publisher.class);
        EventWaitNode<TestRecord, TestRecord> node = new EventWaitNode<>(100, TimeUnit.MILLISECONDS, publisher);

        Flow.Subscription subscription = mock(Flow.Subscription.class);
        doAnswer(invocation -> {
            Flow.Subscriber<TestRecord> subscriber = invocation.getArgument(0);
            subscriber.onSubscribe(subscription);
            return null;
        }).when(publisher).subscribe(any());

        // The apply method will block and then throw a RuntimeException wrapping a TimeoutException
        assertThrows(RuntimeException.class, () -> node.apply(new TestRecord("input")));

        // Verify that even on timeout, the listener was subscribed and then cancelled
        verify(publisher).subscribe(any());
        verify(subscription).cancel();
    }
}

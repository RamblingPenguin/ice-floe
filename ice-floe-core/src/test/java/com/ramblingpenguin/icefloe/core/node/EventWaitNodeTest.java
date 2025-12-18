package com.ramblingpenguin.icefloe.core.node;

import com.ramblingpenguin.glacier.observe.Listener;
import com.ramblingpenguin.glacier.observe.Observable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class EventWaitNodeTest {

    record TestRecord(String value) {}

    @Test
    void testEventReceived() throws InterruptedException {
        @SuppressWarnings("unchecked")
        Observable<Listener<TestRecord>> observable = mock(Observable.class);
        EventWaitNode<TestRecord, TestRecord> node = new EventWaitNode<>(1, TimeUnit.SECONDS, observable);

        // Use ArgumentCaptor to grab the listener when it's added
        ArgumentCaptor<Listener<TestRecord>> listenerCaptor = ArgumentCaptor.forClass(Listener.class);

        // Start the node execution in a separate thread so we can simulate an external event
        Thread executionThread = new Thread(() -> {
            TestRecord result = node.apply(new TestRecord("input"));
            assertEquals("event-data", result.value());
        });
        executionThread.start();

        // Give the execution thread a moment to add the listener
        Thread.sleep(100);

        // Verify the listener was added and capture it
        verify(observable).addListener(listenerCaptor.capture());
        Listener<TestRecord> capturedListener = listenerCaptor.getValue();

        // Simulate the event firing
        capturedListener.onEvent(new TestRecord("event-data"));

        // Wait for the execution thread to finish
        executionThread.join();

        // Verify the listener was removed
        verify(observable).removeListener(capturedListener);
    }

    @Test
    void testTimeout() {
        @SuppressWarnings("unchecked")
        Observable<Listener<TestRecord>> observable = mock(Observable.class);
        EventWaitNode<TestRecord, TestRecord> node = new EventWaitNode<>(100, TimeUnit.MILLISECONDS, observable);

        // The apply method will block and then throw a RuntimeException wrapping a TimeoutException
        assertThrows(RuntimeException.class, () -> node.apply(new TestRecord("input")));

        // Verify that even on timeout, the listener is removed
        verify(observable).addListener(any());
        verify(observable).removeListener(any());
    }
}

package com.ramblingpenguin.icefloe.core.node;

import com.ramblingpenguin.glacier.observe.Listener;
import com.ramblingpenguin.glacier.observe.Observable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EventWaitNodeTest {

    @Test
    void testEventReceived() {
        Observable<Listener<String>> observable = mock(Observable.class);
        EventWaitNode<String, String> node = new EventWaitNode<>(1, TimeUnit.SECONDS, observable);

        ArgumentCaptor<Listener<String>> captor = ArgumentCaptor.forClass(Listener.class);
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            verify(observable).addListener(captor.capture());
            captor.getValue().onEvent("event-data");
        }).start();

        String result = node.apply("input");
        assertEquals("event-data", result);
    }

    @Test
    void testTimeout() {
        Observable<Listener<String>> observable = mock(Observable.class);
        EventWaitNode<String, String> node = new EventWaitNode<>(100, TimeUnit.MILLISECONDS, observable);

        assertThrows(RuntimeException.class, () -> node.apply("input"));
    }
}

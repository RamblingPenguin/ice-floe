package com.ramblingpenguin.icefloe.core.node;

import com.ramblingpenguin.glacier.observe.Listener;
import com.ramblingpenguin.glacier.observe.Observable;
import com.ramblingpenguin.icefloe.core.Node;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EventWaitNode<INPUT, EVENT> implements Node<INPUT, EVENT>, Listener<EVENT> {

    private final long timeout;
    private final TimeUnit timeUnit;
    private final Observable<Listener<EVENT>> observable;
    private final CompletableFuture<EVENT> future;

    public EventWaitNode(long timeout, TimeUnit timeUnit, Observable<Listener<EVENT>> observable) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.observable = observable;
        this.future = new CompletableFuture<>();
    }

    @Override
    public void onEvent(EVENT event) {
        this.future.complete(event);
    }

    @Override
    public EVENT apply(INPUT input) {
        this.observable.addListener(this);
        try {
            return this.future.get(this.timeout, this.timeUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
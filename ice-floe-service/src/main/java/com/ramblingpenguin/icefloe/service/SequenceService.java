package com.ramblingpenguin.icefloe.service;

import com.ramblingpenguin.glacier.lifecycle.Startable;
import com.ramblingpenguin.glacier.lifecycle.Stoppable;
import com.ramblingpenguin.icefloe.core.Node;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A service that manages the lifecycle and execution of registered sequences.
 */
public class SequenceService implements Startable, Stoppable {

    private final SequenceRegistry registry;
    private final ExecutorService executor;
    private boolean isRunning = false;

    public SequenceService() {
        this(new SequenceRegistry(), Executors.newVirtualThreadPerTaskExecutor());
    }

    public SequenceService(SequenceRegistry registry, ExecutorService executor) {
        this.registry = registry;
        this.executor = executor;
    }

    /**
     * Starts the service.
     */
    public void start() {
        if (!isRunning) {
            isRunning = true;
            // In the future, this could involve health checks or other startup logic.
        }
    }

    /**
     * Stops the service, shutting down the underlying executor.
     */
    public void stop() {
        if (isRunning) {
            isRunning = false;
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Registers a sequence with the service.
     *
     * @param id       The unique ID for the sequence.
     * @param sequence The sequence to register.
     */
    public void register(String id, Node<?, ?> sequence) {
        registry.register(id, sequence);
    }

    /**
     * Executes a sequence asynchronously.
     *
     * @param id    The ID of the sequence to execute.
     * @param input The input to the sequence.
     * @return A {@link CompletableFuture} holding the result of the execution.
     * @throws IllegalStateException if the service is not running or the sequence is not found.
     */
    public <I, O> CompletableFuture<O> execute(String id, I input) {
        if (!isRunning) {
            throw new IllegalStateException("Service is not running.");
        }
        Node<I, O> sequence = registry.<I, O>get(id)
                .orElseThrow(() -> new IllegalStateException("Sequence with ID '" + id + "' not found."));

        return CompletableFuture.supplyAsync(() -> sequence.apply(input), executor);
    }

    /**
     * Executes a sequence synchronously, blocking until the result is available.
     *
     * @param id    The ID of the sequence to execute.
     * @param input The input to the sequence.
     * @return The result of the sequence execution.
     * @throws RuntimeException if the execution fails.
     */
    public <I, O> O executeSync(String id, I input) {
        if (!isRunning) {
            throw new IllegalStateException("Service is not running.");
        }
        Node<I, O> sequence = registry.<I, O>get(id)
                .orElseThrow(() -> new IllegalStateException("Sequence with ID '" + id + "' not found."));
        return sequence.apply(input);
    }
}

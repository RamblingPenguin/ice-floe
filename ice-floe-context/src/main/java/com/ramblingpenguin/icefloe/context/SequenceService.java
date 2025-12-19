package com.ramblingpenguin.icefloe.context;

import com.ramblingpenguin.glacier.lifecycle.Startable;
import com.ramblingpenguin.glacier.lifecycle.Stoppable;
import com.ramblingpenguin.icefloe.core.Node;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A service that manages the lifecycle, execution, and persistence of registered sequences.
 * Use the {@link Builder} to construct an instance.
 */
public class SequenceService implements Startable, Stoppable {

    private final SequenceRegistry registry;
    private final ExecutorService executor;
    private final TypeCombinerFactory typeCombinerFactory;
    private final SequenceContextPersistence persistence;
    private final Map<String, SequenceContext> activeContexts = new ConcurrentHashMap<>();
    private boolean isRunning = false;

    private SequenceService(
            SequenceRegistry registry,
            ExecutorService executor,
            TypeCombinerFactory typeCombinerFactory,
            SequenceContextPersistence persistence) {
        this.registry = registry;
        this.executor = executor;
        this.typeCombinerFactory = typeCombinerFactory;
        this.persistence = persistence;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void start() {
        if (!isRunning) {
            isRunning = true;
            if (persistence != null) {
                try {
                    for (String executionId : persistence.listStoredExecutionIds()) {
                        SequenceContext context = persistence.loadState(executionId);
                        if (context != null) {
                            activeContexts.put(executionId, context);
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException("Failed to load persisted contexts", e);
                }
            }
        }
    }

    public void stop() {
        if (isRunning) {
            isRunning = false;
            if (persistence != null) {
                for (Map.Entry<String, SequenceContext> entry : activeContexts.entrySet()) {
                    try {
                        persistence.saveState(entry.getKey(), entry.getValue());
                    } catch (IOException e) {
                        System.err.println("Failed to save context for execution ID: " + entry.getKey());
                    }
                }
            }
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

    public <I> void register(ContextualSequence<I> sequence) {
        registry.register(sequence);
    }

    public <I extends Serializable> CompletableFuture<SequenceContext> execute(SequenceKey<I, SequenceContext> sequenceKey, I input) {
        if (!isRunning) {
            throw new IllegalStateException("Service is not running.");
        }
        Node<I, SequenceContext> sequence = registry.<I, SequenceContext>get(sequenceKey)
                .orElseThrow(() -> new IllegalStateException("Sequence with ID '" + sequenceKey.id() + "' not found."));

        return CompletableFuture.supplyAsync(() -> {
            SequenceContext finalContext = sequence.apply(input);
            activeContexts.put(finalContext.getExecutionId().id(), finalContext);
            return finalContext;
        }, executor);
    }

    public <I extends Serializable> SequenceContext executeSync(SequenceKey<I, SequenceContext> id, I input) {
        if (!isRunning) {
            throw new IllegalStateException("Service is not running.");
        }
        Node<I, SequenceContext> sequence = registry.<I, SequenceContext>get(id)
                .orElseThrow(() -> new IllegalStateException("Sequence with ID '" + id + "' not found."));

        SequenceContext finalContext = sequence.apply(input);
        activeContexts.put(finalContext.getExecutionId().id(), finalContext);
        return finalContext;
    }

    public void saveState(String executionId, SequenceContext context) throws IOException {
        activeContexts.put(executionId, context);
        persistence.saveState(executionId, context);
    }

    public SequenceContext loadState(String executionId) {
        if (activeContexts.containsKey(executionId)) {
            return activeContexts.get(executionId);
        }
        try {
            SequenceContext context = persistence.loadState(executionId);
            if (context != null) {
                activeContexts.put(executionId, context);
            }
            return context;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load context for execution ID: " + executionId, e);
        }
    }

    /**
     * A fluent builder for creating {@link SequenceService} instances.
     */
    public static class Builder {
        private SequenceRegistry registry = new SequenceRegistry();
        private ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        private TypeCombinerFactory typeCombinerFactory = new DefaultTypeCombinerFactory();
        private SequenceContextPersistence persistence;

        public Builder withRegistry(SequenceRegistry registry) {
            this.registry = registry;
            return this;
        }

        public Builder withExecutor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        public Builder withTypeCombinerFactory(TypeCombinerFactory typeCombinerFactory) {
            this.typeCombinerFactory = typeCombinerFactory;
            return this;
        }

        public Builder withPersistence(SequenceContextPersistence persistence) {
            this.persistence = persistence;
            return this;
        }

        public SequenceService build() {
            return new SequenceService(registry, executor, typeCombinerFactory, persistence);
        }
    }
}

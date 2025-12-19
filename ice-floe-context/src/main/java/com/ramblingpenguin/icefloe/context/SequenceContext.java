package com.ramblingpenguin.icefloe.context;

import java.io.*;
import java.util.*;

/**
 * An immutable, thread-safe, and serializable context object that holds the outputs of nodes in a {@code ContextualSequence}.
 * Each modification returns a new {@code SequenceContext} instance.
 */
public class SequenceContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 5L; // Version bump for transient contextMap

    private final ExecutionId executionId;
    private transient SequencedMap<NodeKey<?>, Object> contextMap;
    private transient TypeCombinerFactory typeCombinerFactory;

    /**
     * Creates a new root context for a new execution flow.
     *
     * @param input The initial input value.
     * @param typeCombinerFactory The factory for resolving merge conflicts.
     * @return A new SequenceContext with a root ExecutionId.
     */
    public static <INPUT extends Serializable> SequenceContext newRootContext(NodeKey<INPUT> inputNodeKey, INPUT input, TypeCombinerFactory typeCombinerFactory) {
        return new SequenceContext(ExecutionId.newRoot(), new LinkedHashMap<>(), typeCombinerFactory)
                .put(inputNodeKey, input);
    }

    public static SequenceContext empty(ExecutionId executionId, TypeCombinerFactory factory) {
        return new SequenceContext(executionId, new LinkedHashMap<>(), factory);
    }

    private SequenceContext(ExecutionId executionId, Map<NodeKey<?>, Object> contextMap, TypeCombinerFactory typeCombinerFactory) {
        this.executionId = executionId;
        this.contextMap = new LinkedHashMap<>(contextMap);
        this.typeCombinerFactory = typeCombinerFactory;
    }

    public ExecutionId getExecutionId() {
        return executionId;
    }

    /**
     * Creates a new "child" context for a sub-flow (e.g., a fork).
     * The new context inherits the parent's data but has a derived ExecutionId.
     *
     * @param childSegment The identifier for the child branch (e.g., an index or unique key).
     * @return A new SequenceContext for the sub-flow.
     */
    public SequenceContext createChildContext(String childSegment) {
        ExecutionId childId = this.executionId.createChildId(childSegment);
        return new SequenceContext(childId, this.contextMap, this.typeCombinerFactory);
    }

    public <T> SequenceContext put(NodeKey<T> key, T value) {
        Map<NodeKey<?>, Object> newMap = new LinkedHashMap<>(this.contextMap);
        if (this.contextMap.containsKey(key)) {
            T oldValue = key.outputType().cast(this.contextMap.get(key));
            T newValue = this.typeCombinerFactory.getCombiner(key).apply(oldValue, value);
            newMap.put(key, newValue);
        } else {
            newMap.put(key, value);
        }
        return new SequenceContext(this.executionId, newMap, this.typeCombinerFactory);
    }

    @SuppressWarnings("unchecked")
    public synchronized SequenceContext merge(SequenceContext other) {
        if (this.contextMap.isEmpty()) {
            return new SequenceContext(this.executionId, other.contextMap, this.typeCombinerFactory);
        }
        if (other.contextMap.isEmpty()) {
            return this;
        }

        Map<NodeKey<?>, Object> newMap = new LinkedHashMap<>(this.contextMap);
        for (Map.Entry<NodeKey<?>, Object> entry : other.contextMap.entrySet()) {
            newMap.merge(entry.getKey(), entry.getValue(), (oldValue, newValue) -> {
                NodeKey<Object> key = (NodeKey<Object>) entry.getKey();
                return this.typeCombinerFactory.getCombiner(key).apply(oldValue, newValue);
            });
        }
        return new SequenceContext(this.executionId, newMap, this.typeCombinerFactory);
    }

    public <T> Optional<T> get(NodeKey<T> key) {
        Object value = contextMap.get(key);
        if (key.outputType().isInstance(value)) {
            return Optional.of(key.outputType().cast(value));
        }
        return Optional.empty();
    }

    public <T> void remove(NodeKey<T> key) {
        contextMap.remove(key);
    }

    public Map<NodeKey<?>, Object> getContext() {
        return Collections.unmodifiableMap(this.contextMap);
    }

    public NodeKey<?> getLastNodeExecuted() {
        var lastEntry = this.contextMap.lastEntry();
        if (lastEntry == null) {
            throw new NoSuchElementException("Cannot get last executed node from an empty context.");
        }
        return lastEntry.getKey();
    }

    // --- Custom Serialization ---

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject(); // Writes serialVersionUID and the final executionId field.
        Map<NodeKey<?>, Object> serializableMap = new LinkedHashMap<>();
        for (Map.Entry<NodeKey<?>, Object> entry : this.contextMap.entrySet()) {
            if (entry.getValue() instanceof Serializable) {
                serializableMap.put(entry.getKey(), entry.getValue());
            }
        }
        out.writeObject(serializableMap);
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject(); // Reads serialVersionUID and the final executionId field.
        this.contextMap = new LinkedHashMap<>((Map<NodeKey<?>, Object>) in.readObject());
        this.typeCombinerFactory = new DefaultTypeCombinerFactory();
    }
}

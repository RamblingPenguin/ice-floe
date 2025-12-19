package com.ramblingpenguin.icefloe.core.context;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.function.BiFunction;

/**
 * An immutable, thread-safe, and serializable context object that holds the outputs of nodes in a {@code ContextualSequence}.
 * Each modification (e.g., `put`, `merge`) returns a new {@code SequenceContext} instance,
 * leaving the original unchanged.
 * <p>
 * To support persistence, all values stored in the context **must** implement {@link Serializable}.
 */
public class SequenceContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 2L; // Bumped version due to serialization changes
    public static final String INITIAL_INPUT_KEY_NAME = "initial";

    /**
     * Creates a new context pre-populated with an initial input value.
     *
     * @param input The initial input value, which must be {@link Serializable}.
     * @param <INPUT> The type of the initial input.
     * @return A new SequenceContext containing the initial value.
     */
    public static <INPUT extends Serializable> SequenceContext fromInitialInput(INPUT input) {
        return fromInitialInput(input, new DefaultTypeCombinerFactory());
    }

    /**
     * Creates a new context pre-populated with an initial input value and a custom combiner factory.
     *
     * @param input The initial input value, which must be {@link Serializable}.
     * @param typeCombinerFactory The factory to use for resolving merge conflicts.
     * @param <INPUT> The type of the initial input.
     * @return A new SequenceContext containing the initial value.
     */
    public static <INPUT extends Serializable> SequenceContext fromInitialInput(INPUT input, TypeCombinerFactory typeCombinerFactory) {
        @SuppressWarnings("unchecked") // Safe cast as input is of type INPUT
        NodeKey<INPUT> inputNodeKey = new NodeKey<>(INITIAL_INPUT_KEY_NAME, (Class<INPUT>) input.getClass());
        return new SequenceContext(typeCombinerFactory).put(inputNodeKey, input);
    }

    /**
     * Returns a globally shared, empty context with a default combiner factory.
     * @return An empty SequenceContext.
     */
    public static SequenceContext empty() {
        return EMPTY_CONTEXT;
    }

    private static final SequenceContext EMPTY_CONTEXT = new SequenceContext();

    private final SequencedMap<NodeKey<?>, Object> contextMap;
    private transient TypeCombinerFactory typeCombinerFactory;

    private SequenceContext(Map<NodeKey<?>, Object> contextMap, TypeCombinerFactory typeCombinerFactory) {
        this.contextMap = new LinkedHashMap<>(contextMap);
        this.typeCombinerFactory = typeCombinerFactory;
    }

    public SequenceContext(TypeCombinerFactory typeCombinerFactory) {
        this(new LinkedHashMap<>(), typeCombinerFactory);
    }

    private SequenceContext() {
        this(new LinkedHashMap<>(), new DefaultTypeCombinerFactory());
    }

    /**
     * Returns a new context with the given key-value pair added or merged.
     *
     * @param key   The key to store the value against.
     * @param value The value to store or merge. Must be {@link Serializable}.
     * @return A new, updated SequenceContext instance.
     * @throws IllegalArgumentException if the value is not serializable.
     */
    public <T extends Serializable> SequenceContext put(NodeKey<T> key, T value) {
        Map<NodeKey<?>, Object> newMap = new LinkedHashMap<>(this.contextMap);
        if (this.contextMap.containsKey(key)) {
            T oldValue = key.outputType().cast(this.contextMap.get(key));
            T newValue = this.typeCombinerFactory.getCombiner(key).apply(oldValue, value);
            newMap.put(key, newValue);
        } else {
            newMap.put(key, value);
        }
        return new SequenceContext(newMap, this.typeCombinerFactory);
    }

    /**
     * Merges another context into this one, returning a new, combined context.
     *
     * @param other The other context to merge.
     * @return A new SequenceContext containing the merged data.
     */
    public SequenceContext merge(SequenceContext other) {
        if (this.contextMap.isEmpty()) return other;
        if (other.contextMap.isEmpty()) return this;

        Map<NodeKey<?>, Object> newMap = new LinkedHashMap<>(this.contextMap);
        for (Map.Entry<NodeKey<?>, Object> entry : other.contextMap.entrySet()) {
            newMap.merge(entry.getKey(), entry.getValue(), (oldValue, newValue) -> {
                @SuppressWarnings("unchecked")
                NodeKey<Object> key = (NodeKey<Object>) entry.getKey();
                return this.typeCombinerFactory.getCombiner(key).apply(oldValue, newValue);
            });
        }
        return new SequenceContext(newMap, this.typeCombinerFactory);
    }

    /**
     * Gets a value from the context.
     *
     * @param key The key of the value to retrieve.
     * @param <T> The type of the value.
     * @return An Optional containing the value if it exists.
     */
    public <T> Optional<T> get(NodeKey<T> key) {
        Object value = contextMap.get(key);
        if (key.outputType().isInstance(value)) {
            return Optional.of(key.outputType().cast(value));
        }
        return Optional.empty();
    }

    /**
     * Returns an unmodifiable view of the underlying map.
     */
    public Map<NodeKey<?>, Object> getContext() {
        return Collections.unmodifiableMap(this.contextMap);
    }

    /**
     * Returns the key of the last node that was executed.
     *
     * @throws NoSuchElementException if the context is empty.
     */
    public NodeKey<?> getLastNodeExecuted() {
        var lastEntry = this.contextMap.lastEntry();
        if (lastEntry == null) {
            throw new NoSuchElementException("Cannot get last executed node from an empty context.");
        }
        return lastEntry.getKey();
    }

    // --- Custom Serialization ---

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Restore the transient factory with a default implementation upon deserialization.
        this.typeCombinerFactory = new DefaultTypeCombinerFactory();
    }
}

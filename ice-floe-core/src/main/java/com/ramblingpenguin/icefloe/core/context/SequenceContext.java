package com.ramblingpenguin.icefloe.core.context;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * An immutable, thread-safe context object that holds the outputs of nodes in a {@link ContextualSequence}.
 * Each modification (e.g., `put`, `merge`) returns a new {@code SequenceContext} instance,
 * leaving the original unchanged.
 */
public class SequenceContext {

    public static final String INITIAL_INPUT_KEY_NAME = "initial";

    /**
     * Creates a new context pre-populated with an initial input value.
     * The value is stored under the key "initial".
     *
     * @param input The initial input value.
     * @param <INPUT> The type of the initial input.
     * @return A new SequenceContext containing the initial value.
     */
    public static <INPUT> SequenceContext fromInitialInput(INPUT input) {
        NodeKey<INPUT> inputNodeKey = new NodeKey<>(INITIAL_INPUT_KEY_NAME, (Class<INPUT>) input.getClass());
        return new SequenceContext().put(inputNodeKey, input);
    }

    /**
     * Returns a globally shared, empty context.
     * @return An empty SequenceContext.
     */
    public static SequenceContext empty() {
        return EMPTY_CONTEXT;
    }

    private static final SequenceContext EMPTY_CONTEXT = new SequenceContext();

    private final Map<NodeKey<?>, Object> contextMap;

    private SequenceContext() {
        this.contextMap = Collections.emptyMap();
    }

    private SequenceContext(Map<NodeKey<?>, Object> contextMap) {
        this.contextMap = Collections.unmodifiableMap(contextMap);
    }

    /**
     * Returns a new context with the given key-value pair added or merged.
     * <p>
     * If the key does not exist, it is added. If the key already exists, the
     * {@link BiFunction combiner} from the {@link NodeKey} is used to merge the old and new values.
     *
     * @param key   The key to store the value against.
     * @param value The value to store or merge.
     * @return A new, updated SequenceContext instance.
     */
    public <T> SequenceContext put(NodeKey<T> key, T value) {
        Map<NodeKey<?>, Object> newMap = new HashMap<>(this.contextMap);
        if (this.contextMap.containsKey(key)) {
            T oldValue = key.outputType().cast(this.contextMap.get(key));
            T newValue = key.combiner().apply(oldValue, value);
            newMap.put(key, newValue);
        } else {
            newMap.put(key, value);
        }
        return new SequenceContext(newMap);
    }

    /**
     * Merges another context into this one, returning a new, combined context.
     * This method is optimized to reduce map copying. Key collisions are resolved
     * using the combiner associated with each {@link NodeKey}.
     *
     * @param other The other context to merge.
     * @return A new SequenceContext containing the merged data.
     */
    public SequenceContext merge(SequenceContext other) {
        if (this.contextMap.isEmpty()) return other;
        if (other.contextMap.isEmpty()) return this;

        Map<NodeKey<?>, Object> newMap = new HashMap<>(this.contextMap);
        for (Map.Entry<NodeKey<?>, Object> entry : other.contextMap.entrySet()) {
            newMap.merge(entry.getKey(), entry.getValue(), (oldValue, newValue) -> {
                @SuppressWarnings("unchecked")
                NodeKey<Object> key = (NodeKey<Object>) entry.getKey();
                return key.combiner().apply(oldValue, newValue);
            });
        }
        return new SequenceContext(newMap);
    }

    /**
     * Gets a value from the context. The lookup is based on the `id` of the {@link NodeKey}.
     *
     * @param key The key of the value to retrieve.
     * @param <T> The type of the value.
     * @return An Optional containing the value if it exists and is of the correct type.
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
     *
     * @return The context map.
     */
    @SuppressWarnings("unused")
    public Map<NodeKey<?>, Object> getContext() {
        return this.contextMap;
    }
}

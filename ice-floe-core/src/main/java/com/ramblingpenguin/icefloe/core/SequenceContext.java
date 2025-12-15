package com.ramblingpenguin.icefloe.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A context object that holds the outputs of nodes in a ContextualSequence.
 * Nodes can retrieve the outputs of other nodes from this context using a NodeKey.
 */
public class SequenceContext {

    public static final NodeKey INITIAL_NODE_KEY = new NodeKey("initial");

    public static <INPUT extends Record> SequenceContext fromInitialInput(INPUT input) {
        SequenceContext context = new SequenceContext();
        context.put(INITIAL_NODE_KEY, input);
        return context;
    }

    private final Map<NodeKey, Record> contextMap = new HashMap<>();

    private SequenceContext() {

    }

    /**
     * Puts a value into the context.
     * @param key The key to store the value against.
     * @param value The value to store.
     */
    public <T extends Record> void put(NodeKey key, T value) {
        if (contextMap.put(key, value) != null) {
            throw new IllegalStateException("Key already exists in context");
        }
    }

    /**
     * Gets a value from the context, casting it to the desired type.
     * @param key The key of the value to retrieve.
     * @param type The class of the type to cast the value to.
     * @param <T> The type to cast the value to.
     * @return An Optional containing the value if it exists and is of the correct type, otherwise an empty Optional.
     */
    public <T> Optional<T> get(NodeKey key, Class<T> type) {
        Object value = contextMap.get(key);
        if (type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }

    @SuppressWarnings("unused")
    public Map<NodeKey, ? extends Record> getContext() {
        return this.contextMap;
    }
}

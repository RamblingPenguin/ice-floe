package com.ramblingpenguin.icefloe.service;

import com.ramblingpenguin.icefloe.core.Node;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe registry for storing and retrieving named {@link com.ramblingpenguin.icefloe.core.Sequence} instances.
 */
public class SequenceRegistry {

    private final Map<String, Node<?, ?>> sequences = new ConcurrentHashMap<>();

    /**
     * Registers a sequence with a given ID.
     *
     * @param id       The unique ID for the sequence.
     * @param sequence The sequence to register.
     * @throws IllegalStateException if a sequence with the same ID is already registered.
     */
    public void register(String id, Node<?, ?> sequence) {
        if (sequences.putIfAbsent(id, sequence) != null) {
            throw new IllegalStateException("A sequence with ID '" + id + "' is already registered.");
        }
    }

    /**
     * Retrieves a sequence by its ID.
     *
     * @param id The ID of the sequence to retrieve.
     * @return An {@link Optional} containing the sequence if found, otherwise empty.
     */
    @SuppressWarnings("unchecked")
    public <I, O> Optional<Node<I, O>> get(String id) {
        return Optional.ofNullable((Node<I, O>) sequences.get(id));
    }
}

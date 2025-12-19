package com.ramblingpenguin.icefloe.context;

import java.util.Objects;
import java.util.UUID;

/**
 * A type-safe key for storing and retrieving values in a {@link SequenceContext}.
 * <p>
 * The key's identity is determined solely by its {@code id}. The {@code outputType}
 * and {@code combiner} are used for type-safe operations but do not affect equality.
 *
 * @param id         The unique identifier for this key.
 * @param outputType The class of the value associated with this key.
 * @param <OUTPUT_TYPE> The type of the value associated with this key.
 */
public record NodeKey<OUTPUT_TYPE>(
        String id,
        Class<OUTPUT_TYPE> outputType
) {
    /**
     * Creates a new NodeKey with a specific ID and a custom combiner.
     *
     * @param id         The unique identifier.
     * @param outputType The class of the value.
     */
    public NodeKey(String id, Class<OUTPUT_TYPE> outputType) {
        this.id = Objects.requireNonNull(id);
        this.outputType = Objects.requireNonNull(outputType);
    }

    /**
     * Creates a new NodeKey with a given ID and type, using the default combiner.
     */
    public static <T> NodeKey<T> of(String id, Class<T> type) {
        return new NodeKey<>(id, type);
    }

    /**
     * Creates a new NodeKey with a random UUID as the ID and a default combiner.
     */
    public static <T> NodeKey<T> of(Class<T> type) {
        return new NodeKey<>(UUID.randomUUID().toString(), type);
    }

    /**
     * Determines equality based solely on the {@code id}.
     * The {@code outputType} and {@code combiner} do not affect equality.
     */
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeKey<?> nodeKey = (NodeKey<?>) o;
        return id.equals(nodeKey.id);
    }

    /**
     * Generates a hash code based solely on the {@code id}.
     */
    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}

package com.ramblingpenguin.icefloe.core.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;

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

    private static final BiFunction<?, ?, ?> THROWING_COMBINER = (v1, v2) -> {
        if (Objects.equals(v1, v2)) return v1; // If values are the same, no conflict.
        throw new IllegalStateException("Duplicate key with different values and no merge combiner specified.");
    };

    @SuppressWarnings("unchecked")
    private static <T> BiFunction<T, T, T> getDefaultCombiner(Class<T> type) {
        if (Collection.class.isAssignableFrom(type)) {
            // If the type is a collection, default to merging the collections.
            return (v1, v2) -> {
                Collection<Object> c1 = new ArrayList<>((Collection<?>) v1);
                c1.addAll((Collection<?>) v2);
                return (T) c1;
            };
        }
        // Otherwise, default to throwing an exception on conflict.
        return (BiFunction<T, T, T>) THROWING_COMBINER;
    }

    /**
     * Creates a new NodeKey with a specific ID and a custom combiner.
     *
     * @param id         The unique identifier.
     * @param outputType The class of the value.
     */
    public NodeKey(String id, Class<OUTPUT_TYPE> outputType) {
        this.id = id;
        this.outputType = outputType;
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

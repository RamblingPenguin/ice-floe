package com.ramblingpenguin.icefloe.context;

import java.io.Serializable;
import java.util.UUID;

/**
 * A serializable, hierarchical identifier for a specific execution flow.
 * The ID is a simple string, but it supports creating derived "child" IDs for sub-flows,
 * such as those in a fork-join operation.
 *
 * @param id The string representation of the ID.
 */
public record ExecutionId(String id) implements Serializable {

    /**
     * Creates a new root ExecutionId with a random UUID.
     *
     * @return A new ExecutionId.
     */
    public static ExecutionId newRoot() {
        return new ExecutionId(UUID.randomUUID().toString());
    }

    /**
     * Creates a new child ExecutionId by appending a segment to the current ID.
     *
     * @param childSegment The segment to append, representing the child execution.
     * @return A new ExecutionId with a derived ID (e.g., "parent-id:child-segment").
     */
    public ExecutionId createChildId(String childSegment) {
        return new ExecutionId(id + ":" + childSegment);
    }
}

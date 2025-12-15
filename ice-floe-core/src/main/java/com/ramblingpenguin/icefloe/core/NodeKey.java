package com.ramblingpenguin.icefloe.core;

import java.util.Objects;

/**
 * A key used to identify a node's output in the {@link SequenceContext}.
 * This class can be extended to include additional metadata about the node or its output.
 */
public class NodeKey {

    private final String id;

    public NodeKey(String id) {
        this.id = Objects.requireNonNull(id, "NodeKey id cannot be null");
    }

    public static NodeKey of(String id) {
        return new NodeKey(id);
    }

    /**
     * Returns the string representation of this key, which is used for storage in the context.
     */
    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeKey nodeKey = (NodeKey) o;
        return Objects.equals(id, nodeKey.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

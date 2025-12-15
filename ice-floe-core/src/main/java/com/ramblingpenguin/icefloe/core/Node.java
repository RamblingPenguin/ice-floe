package com.ramblingpenguin.icefloe.core;

import java.util.function.Function;

@FunctionalInterface
public interface Node<INPUT, OUTPUT> extends Function<INPUT, OUTPUT> {
    /**
     * Accepts a visitor.
     * @param visitor The visitor to accept.
     */
    default void accept(IceFloeVisitor visitor) {
        visitor.visitNode(this);
    }
}

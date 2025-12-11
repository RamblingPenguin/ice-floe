package com.ramblingpenguin.icefloe.core;

import java.util.function.Function;

/**
 * The fundamental building block of the Ice Floe library.
 * <p>
 * A {@code Node} represents a processing step that transforms an input of type {@code INPUT}
 * into an output of type {@code OUTPUT}. It extends the standard Java {@link Function} interface,
 * allowing nodes to be easily composed or used in lambda expressions.
 *
 * @param <INPUT>  The type of the input to this node.
 * @param <OUTPUT> The type of the result produced by this node.
 */
@FunctionalInterface
public interface Node<INPUT, OUTPUT> extends Function<INPUT, OUTPUT> {
    
    /**
     * Accepts a visitor for inspecting the node structure.
     * 
     * @param visitor the visitor.
     */
    default void accept(IceFloeVisitor visitor) {
        visitor.visitNode(this);
    }
}
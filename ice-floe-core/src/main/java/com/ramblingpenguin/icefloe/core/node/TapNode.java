package com.ramblingpenguin.icefloe.core.node;

import com.ramblingpenguin.icefloe.core.Node;

import java.util.function.Consumer;

public class TapNode<TYPE> implements Node<TYPE, TYPE> {

    private final Consumer<TYPE> tap;

    public TapNode(Consumer<TYPE> tap) {
        this.tap = tap;
    }

    @Override
    public TYPE apply(TYPE type) {
        this.tap.accept(type);
        return type;
    }
}

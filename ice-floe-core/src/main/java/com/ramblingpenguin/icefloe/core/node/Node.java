package com.ramblingpenguin.icefloe.core.node;

import java.util.function.Function;

@FunctionalInterface
public interface Node<INPUT, OUTPUT> extends Function<INPUT, OUTPUT> {

}

package com.ramblingpenguin.icefloe.core;

import java.util.function.Function;

@FunctionalInterface
public interface Node<INPUT, OUTPUT> extends Function<INPUT, OUTPUT> {

}

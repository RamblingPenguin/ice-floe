package com.ramblingpenguin.icefloe.context;

import java.util.function.BiFunction;

public interface TypeCombinerFactory {

    <T> BiFunction<T, T, T> getCombiner(NodeKey<T> type);

    <T> void registerCombiner(NodeKey<T> type, BiFunction<T, T, T> combiner);

    <T> void registerCombiner(Class<T> type, BiFunction<T, T, T> combiner);
}

package com.ramblingpenguin.icefloe.core.context;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultTypeCombinerFactory implements TypeCombinerFactory {

    private static final Map<Class<?>, BiFunction<?, ?, ?>> DEFAULT_BY_CLASS_COMBINERS = new HashMap<>() {{
        put(Collection.class, (BiFunction<Collection, Collection, Collection>)
                (c1, c2) -> Stream.concat(c1.stream(), c2.stream()).toList());
        put(List.class, (BiFunction<List, List, List>) (l1,l2) -> Stream.concat(l1.stream(),l2.stream()).toList());
        put(ArrayList.class, (BiFunction<ArrayList<?>, ArrayList<?>, ArrayList<?>>) (l1,l2) -> new ArrayList<>(Stream.concat(l1.stream(), l2.stream()).toList()));
        put(Set.class, (BiFunction<Set, Set, Set>) (s1,s2) -> (Set) Stream.concat(s1.stream(),s2.stream()).collect(Collectors.toSet()));
        put(HashMap.class, (BiFunction<Map, Map, Map>) (m1, m2) -> {
            Map<?, ?> newMap = new HashMap<>(m1);
            newMap.putAll(m2);
            return newMap;
        });
    }};

    private static final BiFunction<?, ?, ?> EXCEPTION_BI_FUNCTION = (o1, o2) -> {
        if (Objects.equals(o1, o2)) return o1; // If values are the same, no conflict.
        throw new IllegalStateException("No combiner registered for type " + o1.getClass().getName() + " and values are not equal.");
    };
    private final Map<NodeKey<?>, BiFunction<?, ?, ?>> nodeSpecificCombiners = new HashMap<>();
    private final Map<Class<?>, BiFunction<?, ?, ?>> byClassCombiners = new HashMap<>();

    public DefaultTypeCombinerFactory() {
        this.initializeDefaultCombiners();
    }

    /**
     * Registers default combiner logic for common Java types.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void initializeDefaultCombiners() {
        this.byClassCombiners.putAll(DEFAULT_BY_CLASS_COMBINERS);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> BiFunction<T, T, T> getCombiner(NodeKey<T> key) {
        // Priority 1: Node-specific combiner
        if (nodeSpecificCombiners.containsKey(key)) {
            return (BiFunction<T, T, T>) nodeSpecificCombiners.get(key);
        }

        // Priority 2: Look for exact class mapping
        if (byClassCombiners.containsKey(key.outputType())) {
            return (BiFunction<T, T, T>) byClassCombiners.get(key.outputType());
        }

        // Priority 3: Find the most specific, assignable class-based combiner
        Class<?> inputType = key.outputType();
        Class<?> bestMatchClass = null;
        BiFunction<?, ?, ?> bestMatchCombiner = null;

        for (Map.Entry<Class<?>, BiFunction<?, ?, ?>> entry : byClassCombiners.entrySet()) {
            Class<?> registeredClass = entry.getKey();
            if (registeredClass.isAssignableFrom(inputType)) {
                // It's a potential match. Is it better than the one we have?
                // A match is "better" if it's more specific (i.e., a subclass of the current best match).
                if (bestMatchClass == null || bestMatchClass.isAssignableFrom(registeredClass)) {
                    bestMatchClass = registeredClass;
                    bestMatchCombiner = entry.getValue();
                }
            }
        }

        if (bestMatchCombiner != null) {
            return (BiFunction<T, T, T>) bestMatchCombiner;
        }

        // Priority 3: Default to the exception-throwing combiner
        return (BiFunction<T, T, T>) EXCEPTION_BI_FUNCTION;
    }

    @Override
    public <T> void registerCombiner(NodeKey<T> key, BiFunction<T, T, T> combiner) {
        this.nodeSpecificCombiners.put(key, combiner);
    }

    @Override
    public <T> void registerCombiner(Class<T> type, BiFunction<T, T, T> combiner) {
        this.byClassCombiners.put(type, combiner);
    }
}

package com.ramblingpenguin.icefloe.core;

import com.ramblingpenguin.icefloe.core.node.ForkSequence;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ForkSequenceTest {

    @Test
    public void testSequentialFork() {
        Node<String, Integer> forkSequence = ForkSequence
                .<String, Integer, Integer>builder(
                        s -> List.of(1, 2, 3, 4, 5),
                        i -> i * 2
                )
                .withReducer(0, Integer::sum)
                .build();

        Integer result = forkSequence.apply("start");
        assertEquals(30, result);
    }

    @Test
    public void testParallelFork() {
        Node<String, Integer> forkSequence = ForkSequence
                .<String, Integer, Integer>builder(
                        s -> List.of(1, 2, 3, 4, 5),
                        i -> i * 2
                )
                .withReducer(0, Integer::sum)
                .parallel()
                .build();

        Integer result = forkSequence.apply("start");
        assertEquals(30, result);
    }

    @Test
    public void testReducerWithSideEffects() {
        AtomicInteger accumulator = new AtomicInteger(0);

        Node<String, AtomicInteger> forkSequence = ForkSequence
                .<String, Integer, Integer>builder(
                        s -> List.of(1, 2, 3, 4, 5),
                        i -> i * 2
                )
                .withReducer(accumulator, (acc, val) -> {
                    acc.addAndGet(val);
                    return acc;
                })
                .build();

        AtomicInteger result = forkSequence.apply("start");
        assertEquals(30, result.get());
    }

    @Test
    public void testBuilderThrowsExceptionWithoutReducer() {
        assertThrows(IllegalStateException.class, () -> {
            ForkSequence.builder(s -> List.of(1, 2, 3), i -> i)
                    .build();
        });
    }
}

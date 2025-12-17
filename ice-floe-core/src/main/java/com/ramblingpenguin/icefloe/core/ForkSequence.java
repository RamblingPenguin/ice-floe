package com.ramblingpenguin.icefloe.core;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A composite {@link Node} that implements a "scatter-gather" or "map-reduce" pattern.
 * <p>
 * This node splits a single input into a collection of items, processes each item independently
 * (potentially in parallel), and then reduces the results back into a single output.
 * <p>
 * Key features:
 * <ul>
 *     <li><b>Input Mapper:</b> Splits the initial input into a collection of {@code FORK_INPUT}s.</li>
 *     <li><b>Fork Node:</b> A {@code Node} that processes each {@code FORK_INPUT} to produce a {@code FORK_OUTPUT}.</li>
 *     <li><b>Reducer:</b> Aggregates all {@code FORK_OUTPUT}s into a final {@code OUTPUT}.</li>
 *     <li><b>Parallelism:</b> Can optionally execute the fork steps in parallel using an {@link Executor}.
 *         By default, it uses a Virtual Thread per task executor if parallel execution is enabled.</li>
 * </ul>
 *
 * @param <INPUT>       The type of the initial input.
 * @param <FORK_INPUT>  The type of the items after splitting the input.
 * @param <FORK_OUTPUT> The type of the result from processing a single item.
 * @param <OUTPUT>      The final aggregated output type.
 */
public class ForkSequence<INPUT, FORK_INPUT, FORK_OUTPUT, OUTPUT> implements Node<INPUT, OUTPUT> {

    private static final Executor DEFAULT_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private final Function<INPUT, Collection<FORK_INPUT>> inputMapper;
    private final Node<FORK_INPUT, FORK_OUTPUT> fork;
    private final Function<INPUT, OUTPUT> initialOutputFactory;
    private final BiFunction<OUTPUT, FORK_OUTPUT, OUTPUT> outputReducer;
    private final boolean isParallel;
    private final Executor executor;


    private ForkSequence(Builder<INPUT, FORK_INPUT, FORK_OUTPUT, OUTPUT> builder) {
        this.inputMapper = builder.inputMapper;
        this.fork = builder.fork;
        this.initialOutputFactory = builder.initialOutputFactory;
        this.outputReducer = builder.outputReducer;
        this.isParallel = builder.isParallel;
        this.executor = builder.executor != null ? builder.executor : DEFAULT_EXECUTOR;
    }

    /**
     * Executes the fork sequence.
     *
     * @param input the initial input.
     * @return the aggregated result.
     */
    @Override
    public OUTPUT apply(INPUT input) {
        Collection<FORK_INPUT> forkInputs = this.inputMapper.apply(input);
        OUTPUT initialOutput = this.initialOutputFactory.apply(input);

        if (isParallel) {
            List<CompletableFuture<FORK_OUTPUT>> futures = forkInputs.stream()
                    .map(forkInput -> CompletableFuture.supplyAsync(() -> fork.apply(forkInput), executor))
                    .collect(Collectors.toList());

            return futures.stream()
                    .map(CompletableFuture::join)
                    .reduce(initialOutput, outputReducer, (a, b) -> {
                        // This combiner is not needed for a sequential reduce, which we are enforcing
                        // to ensure deterministic results, as a reducer function may not be associative.
                        throw new IllegalStateException("Combiner should not be called.");
                    });
        } else {
            return forkInputs.stream()
                    .map(fork::apply)
                    .reduce(initialOutput, outputReducer, (a, b) -> {
                        throw new IllegalStateException("Combiner should not be called.");
                    });
        }
    }

    public Node<FORK_INPUT, FORK_OUTPUT> getForkNode() {
        return fork;
    }

    /**
     * Creates a new Builder for a ForkSequence.
     *
     * @param inputMapper The function to split the input into a collection of items.
     * @param fork        The node to process each item.
     * @param <INPUT>     The initial input type.
     * @param <FORK_INPUT> The type of split items.
     * @param <FORK_OUTPUT> The type of item results.
     * @return A new Builder.
     */
    public static <INPUT, FORK_INPUT, FORK_OUTPUT> Builder<INPUT, FORK_INPUT, FORK_OUTPUT, ?> builder(
            Function<INPUT, Collection<FORK_INPUT>> inputMapper,
            Node<FORK_INPUT, FORK_OUTPUT> fork) {
        return new Builder<>(inputMapper, fork);
    }

    /**
     * Builder for configuring {@link ForkSequence}.
     */
    public static class Builder<INPUT, FORK_INPUT, FORK_OUTPUT, OUTPUT> {
        private final Function<INPUT, Collection<FORK_INPUT>> inputMapper;
        private final Node<FORK_INPUT, FORK_OUTPUT> fork;
        private Function<INPUT, OUTPUT> initialOutputFactory;
        private BiFunction<OUTPUT, FORK_OUTPUT, OUTPUT> outputReducer;
        private boolean isParallel = false;
        private Executor executor = null;

        public Builder(Function<INPUT, Collection<FORK_INPUT>> inputMapper, Node<FORK_INPUT, FORK_OUTPUT> fork) {
            this.inputMapper = inputMapper;
            this.fork = fork;
        }

        /**
         * Configures the reducer for aggregating results, using a static initial value.
         *
         * @param initialOutput The initial value for the reduction (identity).
         * @param outputReducer A function that combines the accumulated result with a new fork output.
         * @param <NEW_OUTPUT>  The type of the final output.
         * @return The builder typed with the new output.
         */
        @SuppressWarnings("unchecked")
        public <NEW_OUTPUT> Builder<INPUT, FORK_INPUT, FORK_OUTPUT, NEW_OUTPUT> withReducer(
                NEW_OUTPUT initialOutput,
                BiFunction<NEW_OUTPUT, FORK_OUTPUT, NEW_OUTPUT> outputReducer) {
            this.initialOutputFactory = (input) -> (OUTPUT) initialOutput;
            this.outputReducer = (BiFunction<OUTPUT, FORK_OUTPUT, OUTPUT>) outputReducer;
            return (Builder<INPUT, FORK_INPUT, FORK_OUTPUT, NEW_OUTPUT>) this;
        }

        /**
         * Configures the reducer for aggregating results, using an initial value derived from the input.
         *
         * @param initialOutputFactory A function to create the initial value based on the input.
         * @param outputReducer        A function that combines the accumulated result with a new fork output.
         * @param <NEW_OUTPUT>         The type of the final output.
         * @return The builder typed with the new output.
         */
        @SuppressWarnings("unchecked")
        public <NEW_OUTPUT> Builder<INPUT, FORK_INPUT, FORK_OUTPUT, NEW_OUTPUT> withReducerFactory(
                Function<INPUT, NEW_OUTPUT> initialOutputFactory,
                BiFunction<NEW_OUTPUT, FORK_OUTPUT, NEW_OUTPUT> outputReducer) {
            this.initialOutputFactory = (Function<INPUT, OUTPUT>) initialOutputFactory;
            this.outputReducer = (BiFunction<OUTPUT, FORK_OUTPUT, OUTPUT>) outputReducer;
            return (Builder<INPUT, FORK_INPUT, FORK_OUTPUT, NEW_OUTPUT>) this;
        }

        /**
         * Enables parallel execution using the default Virtual Thread executor.
         *
         * @return This builder.
         */
        public Builder<INPUT, FORK_INPUT, FORK_OUTPUT, OUTPUT> parallel() {
            this.isParallel = true;
            return this;
        }

        /**
         * Enables parallel execution using a custom {@link Executor}.
         *
         * @param executor The executor to use.
         * @return This builder.
         */
        public Builder<INPUT, FORK_INPUT, FORK_OUTPUT, OUTPUT> parallel(Executor executor) {
            this.isParallel = true;
            this.executor = executor;
            return this;
        }

        /**
         * Builds the {@link ForkSequence}.
         *
         * @return The new ForkSequence instance.
         * @throws IllegalStateException if a reducer has not been configured.
         */
        public ForkSequence<INPUT, FORK_INPUT, FORK_OUTPUT, OUTPUT> build() {
            if (outputReducer == null) {
                throw new IllegalStateException("A reducer must be configured using withReducer()");
            }
            return new ForkSequence<>(this);
        }
    }
}
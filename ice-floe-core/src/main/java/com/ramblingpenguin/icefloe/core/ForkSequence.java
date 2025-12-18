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
 *
 * @param <INPUT>       The type of the initial input.
 * @param <FORK_INPUT>  The type of the items after splitting the input.
 * @param <FORK_OUTPUT> The type of the result from processing a single item.
 * @param <OUTPUT>      The final aggregated output type.
 */
public class ForkSequence<INPUT, FORK_INPUT, FORK_OUTPUT, OUTPUT> implements Node<INPUT, OUTPUT> {

    protected static final Executor DEFAULT_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    protected final Function<INPUT, Collection<FORK_INPUT>> inputMapper;
    protected final Node<FORK_INPUT, FORK_OUTPUT> fork;
    protected final Function<INPUT, OUTPUT> initialOutputFactory;
    protected final BiFunction<OUTPUT, FORK_OUTPUT, OUTPUT> outputReducer;
    protected final boolean isParallel;
    protected final Executor executor;

    protected ForkSequence(Builder<INPUT, FORK_INPUT, FORK_OUTPUT, OUTPUT> builder) {
        this.inputMapper = builder.inputMapper;
        this.fork = builder.fork;
        this.initialOutputFactory = builder.initialOutputFactory;
        this.outputReducer = builder.outputReducer;
        this.isParallel = builder.isParallel;
        this.executor = builder.executor != null ? builder.executor : DEFAULT_EXECUTOR;
    }

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

    public static <INPUT, FORK_INPUT, FORK_OUTPUT> Builder<INPUT, FORK_INPUT, FORK_OUTPUT, ?> builder(
            Function<INPUT, Collection<FORK_INPUT>> inputMapper,
            Node<FORK_INPUT, FORK_OUTPUT> fork) {
        return new Builder<>(inputMapper, fork);
    }

    public static class Builder<INPUT, FORK_INPUT, FORK_OUTPUT, OUTPUT> {
        private Function<INPUT, Collection<FORK_INPUT>> inputMapper;
        private Node<FORK_INPUT, FORK_OUTPUT> fork;
        private Function<INPUT, OUTPUT> initialOutputFactory;
        private BiFunction<OUTPUT, FORK_OUTPUT, OUTPUT> outputReducer;
        private boolean isParallel = false;
        private Executor executor = null;

        public Builder(Function<INPUT, Collection<FORK_INPUT>> inputMapper, Node<FORK_INPUT, FORK_OUTPUT> fork) {
            this.inputMapper = inputMapper;
            this.fork = fork;
        }

        @SuppressWarnings("unchecked")
        public <NEW_OUTPUT> Builder<INPUT, FORK_INPUT, FORK_OUTPUT, NEW_OUTPUT> withReducer(
                NEW_OUTPUT initialOutput,
                BiFunction<NEW_OUTPUT, FORK_OUTPUT, NEW_OUTPUT> outputReducer) {
            this.initialOutputFactory = (input) -> (OUTPUT) initialOutput;
            this.outputReducer = (BiFunction<OUTPUT, FORK_OUTPUT, OUTPUT>) outputReducer;
            return (Builder<INPUT, FORK_INPUT, FORK_OUTPUT, NEW_OUTPUT>) this;
        }

        @SuppressWarnings("unchecked")
        public <NEW_OUTPUT> Builder<INPUT, FORK_INPUT, FORK_OUTPUT, NEW_OUTPUT> withReducerFactory(
                Function<INPUT, NEW_OUTPUT> initialOutputFactory,
                BiFunction<NEW_OUTPUT, FORK_OUTPUT, NEW_OUTPUT> outputReducer) {
            this.initialOutputFactory = (Function<INPUT, OUTPUT>) initialOutputFactory;
            this.outputReducer = (BiFunction<OUTPUT, FORK_OUTPUT, OUTPUT>) outputReducer;
            return (Builder<INPUT, FORK_INPUT, FORK_OUTPUT, NEW_OUTPUT>) this;
        }

        public Builder<INPUT, FORK_INPUT, FORK_OUTPUT, OUTPUT> parallel() {
            this.isParallel = true;
            return this;
        }

        public Builder<INPUT, FORK_INPUT, FORK_OUTPUT, OUTPUT> parallel(Executor executor) {
            this.isParallel = true;
            this.executor = executor;
            return this;
        }

        public ForkSequence<INPUT, FORK_INPUT, FORK_OUTPUT, OUTPUT> build() {
            if (outputReducer == null) {
                throw new IllegalStateException("A reducer must be configured using withReducer()");
            }
            return new ForkSequence<>(this);
        }
    }
}

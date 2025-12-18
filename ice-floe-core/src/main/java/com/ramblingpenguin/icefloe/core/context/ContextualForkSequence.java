package com.ramblingpenguin.icefloe.core.context;

import com.ramblingpenguin.icefloe.core.ForkSequence;
import com.ramblingpenguin.icefloe.core.Node;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * A specialized {@link ForkSequence} for use within a {@link ContextualSequence}.
 * It scatters tasks to be executed in parallel and then gathers the results, merging
 * the resulting {@link SequenceContext}s into a single, unified context.
 *
 * @param <FORK_INPUT> The type of the items after splitting the input from the initial context.
 */
public class ContextualForkSequence<FORK_INPUT> extends ForkSequence<SequenceContext, FORK_INPUT, SequenceContext, SequenceContext> {

    private ContextualForkSequence(ContextualBuilder<FORK_INPUT> builder) {
        super(builder.buildBase());
    }

    /**
     * Creates a new builder for a ContextualForkSequence.
     * This is the dedicated entry point for creating a fork-join sequence that operates on a SequenceContext.
     *
     * @param inputMapper A function to split the initial context into a collection of fork inputs.
     * @param forkNode    The node to execute for each fork input. This node must return a SequenceContext.
     * @param <FORK_INPUT> The type of the items after splitting.
     * @return A new, specialized builder for contextual fork sequences.
     */
    public static <FORK_INPUT> ContextualBuilder<FORK_INPUT> contextualBuilder(
            Function<SequenceContext, Collection<FORK_INPUT>> inputMapper,
            Node<FORK_INPUT, SequenceContext> forkNode) {
        return new ContextualBuilder<>(inputMapper, forkNode);
    }

    /**
     * The specialized builder for {@link ContextualForkSequence}.
     */
    public static class ContextualBuilder<FORK_INPUT> {
        private final Function<SequenceContext, Collection<FORK_INPUT>> inputMapper;
        private final Node<FORK_INPUT, SequenceContext> forkNode;
        private Executor executor = DEFAULT_EXECUTOR;
        private boolean isParallel = true;

        public ContextualBuilder(Function<SequenceContext, Collection<FORK_INPUT>> inputMapper, Node<FORK_INPUT, SequenceContext> forkNode) {
            this.inputMapper = inputMapper;
            this.forkNode = forkNode;
        }

        public ContextualBuilder<FORK_INPUT> withExecutor(Executor executor) {
            this.executor = executor;
            this.isParallel = true;
            return this;
        }

        public ContextualBuilder<FORK_INPUT> sequential() {
            this.isParallel = false;
            return this;
        }

        private ForkSequence.Builder<SequenceContext, FORK_INPUT, SequenceContext, SequenceContext> buildBase() {
            ForkSequence.Builder<SequenceContext, FORK_INPUT, SequenceContext, SequenceContext> baseBuilder =
                    new ForkSequence.Builder<>(inputMapper, forkNode)
                            .withReducerFactory(
                                    initialContext -> initialContext, // The initial output is the initial context
                                    SequenceContext::merge // The reducer is now the merge method
                            );

            if (isParallel) {
                baseBuilder.parallel(executor);
            }

            return baseBuilder;
        }

        public ContextualForkSequence<FORK_INPUT> build() {
            return new ContextualForkSequence<>(this);
        }
    }
}

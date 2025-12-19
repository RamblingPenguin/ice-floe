package com.ramblingpenguin.icefloe.context;

import com.ramblingpenguin.icefloe.core.Node;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A specialized node for use within a {@link ContextualSequence} that implements a "scatter-gather" pattern on a context.
 * It takes an {@link Iterable} from the context, processes each item in a parallel sub-flow, and merges the resulting
 * contexts back into the main flow.
 *
 * @param <ITEM_TYPE> The type of items in the iterable.
 */
public class ContextualForkSequence<ITEM_TYPE> implements Node<SequenceContext, SequenceContext> {

    private static final Executor DEFAULT_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    private final NodeKey<? extends Iterable<ITEM_TYPE>> scatterKey;
    private final NodeKey<ITEM_TYPE> itemKey;
    private final Node<SequenceContext, SequenceContext> forkNode;
    private final Executor executor;
    private final boolean isParallel;

    /**
     * Creates a new ContextualForkSequence.
     *
     * @param scatterKey The key for the {@link Iterable} in the context to scatter.
     * @param itemKey    The key to use for each item in the child contexts.
     * @param forkNode   The node to execute for each item. This node will receive a child context containing the item.
     */
    public ContextualForkSequence(
            NodeKey<? extends Iterable<ITEM_TYPE>> scatterKey,
            NodeKey<ITEM_TYPE> itemKey,
            Node<SequenceContext, SequenceContext> forkNode) {
        this(scatterKey, itemKey, forkNode, DEFAULT_EXECUTOR, true);
    }

    private ContextualForkSequence(
            NodeKey<? extends Iterable<ITEM_TYPE>> scatterKey,
            NodeKey<ITEM_TYPE> itemKey,
            Node<SequenceContext, SequenceContext> forkNode,
            Executor executor,
            boolean isParallel) {
        this.scatterKey = scatterKey;
        this.itemKey = itemKey;
        this.forkNode = forkNode;
        this.executor = executor;
        this.isParallel = isParallel;
    }

    @Override
    public SequenceContext apply(SequenceContext parentContext) {
        if (isParallel) {
            return this.applyInParallel(parentContext);
        } else {
            return this.applySequential(parentContext);
        }
    }

    private Stream<SequenceContext> buildChildContexts(SequenceContext parentContext) {
        AtomicInteger childIndex = new AtomicInteger(0);
        return StreamSupport.stream(parentContext.get(scatterKey).orElseThrow().spliterator(), false)
                .map(item -> {
                    SequenceContext childContext = parentContext.createChildContext(String.valueOf(childIndex.getAndIncrement()));
                    return childContext.put(this.itemKey, item);
                });
    }

    private SequenceContext applySequential(SequenceContext parentContext) {
        return this.buildChildContexts(parentContext)
                .map(this.forkNode)
                .reduce(parentContext, (p, c) -> {
                    c.remove(this.itemKey);
                    p.merge(c);
                    return parentContext;
                });
    }

    private SequenceContext applyInParallel(SequenceContext parentContext) {
        List<CompletableFuture<SequenceContext>> futures = this.buildChildContexts(parentContext)
                .map(childContext ->
                    CompletableFuture.supplyAsync(() -> this.forkNode.apply(childContext), this.executor)
                ).toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .reduce(parentContext, (p, c) -> {
                    c.remove(this.itemKey);
                    return p.merge(c);
                });
    }

    /**
     * Returns a new instance of the fork sequence that will run sequentially.
     */
    public ContextualForkSequence<ITEM_TYPE> sequential() {
        return new ContextualForkSequence<>(scatterKey, itemKey, forkNode, executor, false);
    }

    /**
     * Returns a new instance of the fork sequence that will run in parallel on the specified executor.
     */
    public ContextualForkSequence<ITEM_TYPE> withExecutor(Executor executor) {
        return new ContextualForkSequence<>(scatterKey, itemKey, forkNode, executor, true);
    }
}

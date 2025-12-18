package com.ramblingpenguin.icefloe.core.context;

import com.ramblingpenguin.icefloe.core.Node;

import java.util.UUID;
import java.util.function.Function;

/**
 * A wrapper node that integrates a standard {@link Node} into a {@link ContextualSequence}.
 * It extracts its input from the context, executes the wrapped node, and returns a new
 * context with the output added under a static key.
 *
 * @param <INPUT>  The input type of the wrapped node.
 * @param <OUTPUT> The output type of the wrapped node.
 */
public class ContextualNode<INPUT, OUTPUT> implements Node<SequenceContext, SequenceContext> {

    private final Node<INPUT, OUTPUT> wrappedNode;
    private final Function<SequenceContext, INPUT> inputExtractor;
    private final NodeKey<OUTPUT> nodeKey;

    public ContextualNode(NodeKey<OUTPUT> nodeKey,
                          Function<SequenceContext, INPUT> inputExtractor,
                          Node<INPUT, OUTPUT> wrappedNode) {
        this.nodeKey = nodeKey;
        this.inputExtractor = inputExtractor;
        this.wrappedNode = wrappedNode;
    }

    /**
     * Gets the static key that identifies this node's output.
     * @return The unique static key for this node.
     */
    public NodeKey<OUTPUT> getKey() {
        return this.nodeKey;
    }

    @Override
    public SequenceContext apply(SequenceContext sequenceContext) {
        INPUT input = inputExtractor.apply(sequenceContext);
        OUTPUT output = wrappedNode.apply(input);
        return sequenceContext.put(nodeKey, output);
    }

    /**
     * Creates a {@code ContextualNode} with a specified static key.
     */
    public static <INPUT, OUTPUT> ContextualNode<INPUT, OUTPUT> of(NodeKey<OUTPUT> nodeKey,
                                                                   Function<SequenceContext, INPUT> inputExtractor,
                                                                   Node<INPUT, OUTPUT> wrappedNode) {
        return new ContextualNode<>(nodeKey, inputExtractor, wrappedNode);
    }

    /**
     * Creates a {@code ContextualNode} with a generated, unique static key.
     */
    public static <INPUT, OUTPUT> ContextualNode<INPUT, OUTPUT> of(Class<OUTPUT> outputType,
                                                                   Function<SequenceContext, INPUT> inputExtractor,
                                                                   Node<INPUT, OUTPUT> wrappedNode) {
        return new ContextualNode<>(new NodeKey<>(UUID.randomUUID().toString(), outputType),
                inputExtractor, wrappedNode);
    }
}

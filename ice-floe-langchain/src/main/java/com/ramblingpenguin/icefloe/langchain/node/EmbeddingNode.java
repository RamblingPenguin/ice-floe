package com.ramblingpenguin.icefloe.langchain.node;

import com.ramblingpenguin.icefloe.core.Node;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;

import java.util.function.Function;

/**
 * A node that interacts with a LangChain4j {@link EmbeddingModel}.
 * It can take either a single String or a {@link TextSegment} as input.
 *
 * @param <INPUT> The input type, typically String or TextSegment.
 */
public class EmbeddingNode<INPUT> implements Node<INPUT, Response<Embedding>> {

    private final EmbeddingModel model;
    private final Function<INPUT, Response<Embedding>> modelExecutor;

    private EmbeddingNode(EmbeddingModel model, Function<INPUT, Response<Embedding>> modelExecutor) {
        this.model = model;
        this.modelExecutor = modelExecutor;
    }

    @Override
    public Response<Embedding> apply(INPUT input) {
        return modelExecutor.apply(input);
    }

    /**
     * Creates an EmbeddingNode that accepts a single String.
     *
     * @param model The EmbeddingModel to use.
     * @return A new EmbeddingNode.
     */
    public static EmbeddingNode<String> fromString(EmbeddingModel model) {
        return new EmbeddingNode<>(model, model::embed);
    }

    /**
     * Creates an EmbeddingNode that accepts a TextSegment.
     *
     * @param model The EmbeddingModel to use.
     * @return A new EmbeddingNode.
     */
    public static EmbeddingNode<TextSegment> fromTextSegment(EmbeddingModel model) {
        return new EmbeddingNode<>(model, model::embed);
    }
}

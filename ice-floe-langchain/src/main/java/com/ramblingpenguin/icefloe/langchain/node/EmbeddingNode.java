package com.ramblingpenguin.icefloe.langchain.node;

import com.ramblingpenguin.icefloe.core.Node;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;

import java.util.function.Function;

/**
 * A {@link Node} implementation that wraps a LangChain4j {@link EmbeddingModel}.
 * <p>
 * This node is used to generate vector embeddings from text. It supports inputs as either
 * a raw {@link String} or a structured {@link TextSegment}.
 *
 * @param <INPUT> The type of input this node accepts. Typically {@link String} or {@link TextSegment}.
 */
public class EmbeddingNode<INPUT> implements Node<INPUT, Response<Embedding>> {

    private final EmbeddingModel model;
    private final Function<INPUT, Response<Embedding>> modelExecutor;

    private EmbeddingNode(EmbeddingModel model, Function<INPUT, Response<Embedding>> modelExecutor) {
        this.model = model;
        this.modelExecutor = modelExecutor;
    }

    /**
     * Executes the wrapped {@link EmbeddingModel} to generate an embedding for the input.
     *
     * @param input The text or segment to embed.
     * @return A {@link Response} containing the generated {@link Embedding}.
     */
    @Override
    public Response<Embedding> apply(INPUT input) {
        return modelExecutor.apply(input);
    }

    /**
     * Creates an {@code EmbeddingNode} that accepts a single {@link String}.
     *
     * @param model The {@link EmbeddingModel} instance to use.
     * @return A new {@code EmbeddingNode} accepting {@link String} input.
     */
    public static EmbeddingNode<String> fromString(EmbeddingModel model) {
        return new EmbeddingNode<>(model, model::embed);
    }

    /**
     * Creates an {@code EmbeddingNode} that accepts a {@link TextSegment}.
     *
     * @param model The {@link EmbeddingModel} instance to use.
     * @return A new {@code EmbeddingNode} accepting {@link TextSegment} input.
     */
    public static EmbeddingNode<TextSegment> fromTextSegment(EmbeddingModel model) {
        return new EmbeddingNode<>(model, model::embed);
    }
}

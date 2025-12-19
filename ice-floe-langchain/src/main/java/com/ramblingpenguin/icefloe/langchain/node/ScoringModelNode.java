package com.ramblingpenguin.icefloe.langchain.node;

import com.ramblingpenguin.icefloe.core.Node;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;

import java.util.function.Function;

/**
 * A {@link Node} implementation that wraps a LangChain4j {@link ScoringModel}.
 * <p>
 * This node is used to score or rank text segments against a query. It is commonly used in
 * RAG (Retrieval-Augmented Generation) pipelines for re-ranking retrieved documents.
 *
 * @param <INPUT> The type of input this node accepts. Typically {@link String} or {@link dev.langchain4j.data.segment.TextSegment}.
 */
public class ScoringModelNode<INPUT> implements Node<INPUT, Response<Double>> {

    private final ScoringModel model;
    private final Function<INPUT, Response<Double>> modelExecutor;

    public ScoringModelNode(ScoringModel model, Function<INPUT, Response<Double>> modelExecutor) {
        this.model = model;
        this.modelExecutor = modelExecutor;
    }

    /**
     * Executes the wrapped {@link ScoringModel} to score the input.
     *
     * @param input The text to score.
     * @return A {@link Response} containing the score as a {@link Double}.
     */
    @Override
    public Response<Double> apply(INPUT input) {
        return this.modelExecutor.apply(input);
    }

    /**
     * Creates a {@code ScoringModelNode} that scores a single {@link String} against a fixed query.
     *
     * @param model The {@link ScoringModel} instance to use.
     * @param scoreQuery The query string to score inputs against.
     * @return A new {@code ScoringModelNode} accepting {@link String} input.
     */
    public static ScoringModelNode<String> fromString(ScoringModel model, String scoreQuery) {
        return new ScoringModelNode<>(model, input -> model.score(input, scoreQuery));
    }
}

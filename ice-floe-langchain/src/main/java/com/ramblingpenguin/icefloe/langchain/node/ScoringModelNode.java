package com.ramblingpenguin.icefloe.langchain.node;

import com.ramblingpenguin.icefloe.core.Node;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;

import java.util.function.Function;

/**
 * A node that interacts with a {@link ScoringModel}.
 * It takes a String as input and returns a score as a Double within a {@link Response}.
 */
public class ScoringModelNode<INPUT> implements Node<INPUT, Response<Double>> {

    private final ScoringModel model;
    private final Function<INPUT, Response<Double>> modelExecutor;

    public ScoringModelNode(ScoringModel model, Function<INPUT, Response<Double>> modelExecutor) {
        this.model = model;
        this.modelExecutor = modelExecutor;
    }

    public Response<Double> apply(INPUT input) {
        return this.modelExecutor.apply(input);
    }

    public static ScoringModelNode<String> fromString(ScoringModel model, String scoreQuery) {
        return new ScoringModelNode<>(model, input -> model.score(input, scoreQuery));
    }
}

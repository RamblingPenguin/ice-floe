package com.ramblingpenguin.icefloe.langchain.node;

import com.ramblingpenguin.icefloe.core.Node;
import dev.langchain4j.model.moderation.Moderation;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.model.output.Response;

import java.util.function.Function;

/**
 * A node that interacts with a LangChain4j {@link ModerationModel}.
 * It takes a String as input and returns a {@link Moderation} result.
 */
public class ModerationNode<INPUT> implements Node<INPUT, Response<Moderation>> {

    private final ModerationModel model;
    private final Function<INPUT, Response<Moderation>> modelFunction;

    public ModerationNode(ModerationModel model, Function<INPUT, Response<Moderation>> modelFunction) {
        this.model = model;
        this.modelFunction = modelFunction;
    }

    @Override
    public Response<Moderation> apply(INPUT input) {
        return this.modelFunction.apply(input);
    }

    public static ModerationNode<String> fromString(ModerationModel model) {
        return new ModerationNode<>(model, model::moderate);
    }
}

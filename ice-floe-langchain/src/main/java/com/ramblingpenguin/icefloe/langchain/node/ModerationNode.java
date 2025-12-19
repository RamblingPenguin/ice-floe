package com.ramblingpenguin.icefloe.langchain.node;

import com.ramblingpenguin.icefloe.core.Node;
import dev.langchain4j.model.moderation.Moderation;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.model.output.Response;

import java.util.function.Function;

/**
 * A {@link Node} implementation that wraps a LangChain4j {@link ModerationModel}.
 * <p>
 * This node is used to check text content for safety violations (e.g., hate speech, violence, sexual content).
 * It takes a {@link String} as input and returns a {@link Moderation} result indicating if the content was flagged.
 *
 * @param <INPUT> The type of input this node accepts. Typically {@link String}.
 */
public class ModerationNode<INPUT> implements Node<INPUT, Response<Moderation>> {

    private final ModerationModel model;
    private final Function<INPUT, Response<Moderation>> modelFunction;

    public ModerationNode(ModerationModel model, Function<INPUT, Response<Moderation>> modelFunction) {
        this.model = model;
        this.modelFunction = modelFunction;
    }

    /**
     * Executes the wrapped {@link ModerationModel} to check the input text.
     *
     * @param input The text to moderate.
     * @return A {@link Response} containing the {@link Moderation} result.
     */
    @Override
    public Response<Moderation> apply(INPUT input) {
        return this.modelFunction.apply(input);
    }

    /**
     * Creates a {@code ModerationNode} that accepts a single {@link String}.
     *
     * @param model The {@link ModerationModel} instance to use.
     * @return A new {@code ModerationNode} accepting {@link String} input.
     */
    public static ModerationNode<String> fromString(ModerationModel model) {
        return new ModerationNode<>(model, model::moderate);
    }
}

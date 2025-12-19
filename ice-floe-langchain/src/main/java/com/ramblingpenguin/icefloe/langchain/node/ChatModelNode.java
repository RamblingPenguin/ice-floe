package com.ramblingpenguin.icefloe.langchain.node;

import com.ramblingpenguin.icefloe.core.Node;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.util.List;
import java.util.function.Function;

/**
 * A {@link Node} implementation that wraps a LangChain4j {@link ChatModel}.
 * <p>
 * This node allows you to integrate Large Language Models (LLMs) into your Ice Floe pipelines.
 * It supports inputs as either a simple {@link String} (which is converted to a {@link UserMessage})
 * or a {@link List} of {@link ChatMessage}s for more complex conversation histories.
 *
 * @param <INPUT> The type of input this node accepts. Typically {@link String} or {@code List<ChatMessage>}.
 */
public class ChatModelNode<INPUT> implements Node<INPUT, ChatResponse> {

    private final ChatModel model;
    private final Function<INPUT, ChatResponse> modelExecutor;

    private ChatModelNode(ChatModel model, Function<INPUT, ChatResponse> modelExecutor) {
        this.model = model;
        this.modelExecutor = modelExecutor;
    }

    /**
     * Executes the wrapped {@link ChatModel} with the given input.
     *
     * @param input The input to the model.
     * @return The {@link ChatResponse} from the model.
     */
    @Override
    public ChatResponse apply(INPUT input) {
        return modelExecutor.apply(input);
    }

    /**
     * Creates a {@code ChatModelNode} that accepts a single {@link String} prompt.
     * <p>
     * The input string is wrapped in a {@link UserMessage} before being sent to the model.
     *
     * @param model The {@link ChatModel} instance to use.
     * @return A new {@code ChatModelNode} accepting {@link String} input.
     */
    public static ChatModelNode<String> fromString(ChatModel model) {
        return new ChatModelNode<>(model, s -> model.doChat(ChatRequest.builder()
                .messages(new UserMessage(s))
                .build()));
    }
}

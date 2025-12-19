package com.ramblingpenguin.icefloe.langchain.node;

import com.ramblingpenguin.icefloe.core.Node;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;

import java.util.List;
import java.util.function.Function;

/**
 * A node that interacts with a LangChain4j {@link ChatModel}.
 * It can take either a single String prompt or a List of {@link ChatMessage}s as input.
 *
 * @param <INPUT> The input type, typically String or List<ChatMessage>.
 */
public class ChatModelNode<INPUT> implements Node<INPUT, ChatResponse> {

    private final ChatModel model;
    private final Function<INPUT, ChatResponse> modelExecutor;

    private ChatModelNode(ChatModel model, Function<INPUT, ChatResponse> modelExecutor) {
        this.model = model;
        this.modelExecutor = modelExecutor;
    }

    @Override
    public ChatResponse apply(INPUT input) {
        return modelExecutor.apply(input);
    }

    /**
     * Creates a ChatModelNode that accepts a single String prompt.
     *
     * @param model The ChatModel to use.
     * @return A new ChatModelNode.
     */
    public static ChatModelNode<String> fromString(ChatModel model) {
        return new ChatModelNode<>(model, s -> model.doChat(ChatRequest.builder()
                .messages(new UserMessage(s))
                .build()));
    }
}

package com.ramblingpenguin.icefloe.langchain.node;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatModelNodeTest {

    @Test
    void testFromString() {
        ChatModel model = mock(ChatModel.class);
        String prompt = "Hello";
        String responseContent = "Hi there!";
        ChatResponse response = ChatResponse.builder().aiMessage(AiMessage.from(responseContent)).build();

        when(model.doChat(any(ChatRequest.class))).thenReturn(response);

        ChatModelNode<String> node = ChatModelNode.fromString(model);
        ChatResponse result = node.apply(prompt);

        assertEquals(responseContent, result.aiMessage().text());
    }
}

package com.ramblingpenguin.icefloe.langchain.context;

import com.ramblingpenguin.icefloe.core.context.ContextualNode;
import com.ramblingpenguin.icefloe.core.context.NodeKey;
import com.ramblingpenguin.icefloe.core.context.SequenceContext;
import com.ramblingpenguin.icefloe.langchain.node.ChatModelNode;
import com.ramblingpenguin.icefloe.langchain.node.EmbeddingNode;
import com.ramblingpenguin.icefloe.langchain.node.ModerationNode;
import com.ramblingpenguin.icefloe.langchain.node.ScoringModelNode;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.moderation.Moderation;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;

import java.util.List;
import java.util.function.Function;

/**
 * A utility class providing factory methods to create contextual AI nodes.
 */
public class ContextualAi {

    /**
     * Creates a contextual chat node that takes a String prompt from the context.
     */
    public static ContextualNode<String, ChatResponse> chat(NodeKey<ChatResponse> outputKey,
                                                         Function<SequenceContext, String> inputExtractor,
                                                         ChatModel model) {
        return ContextualNode.of(outputKey, inputExtractor, ChatModelNode.fromString(model));
    }

    /**
     * Creates a contextual chat node that takes a List of ChatMessages from the context.
     */
    public static ContextualNode<List<ChatMessage>, ChatResponse> chatFromMessages(NodeKey<ChatResponse> outputKey,
                                                                                Function<SequenceContext, List<ChatMessage>> inputExtractor,
                                                                                ChatModel model) {
        return ContextualNode.of(outputKey, inputExtractor, ChatModelNode.fromMessages(model));
    }

    /**
     * Creates a contextual embedding node that takes a String from the context.
     */
    public static ContextualNode<String, Response<Embedding>> embed(NodeKey<Response<Embedding>> outputKey,
                                                          Function<SequenceContext, String> inputExtractor,
                                                          EmbeddingModel model) {
        return ContextualNode.of(outputKey, inputExtractor, EmbeddingNode.fromString(model));
    }

    /**
     * Creates a contextual moderation node that takes a String from the context.
     */
    public static ContextualNode<String, Response<Moderation>> moderate(NodeKey<Response<Moderation>> outputKey,
                                                              Function<SequenceContext, String> inputExtractor,
                                                              ModerationModel model) {
        return ContextualNode.of(outputKey, inputExtractor, ModerationNode.fromString(model));
    }

    /**
     * Creates a contextual scoring node that takes a String from the context.
     */
    public static ContextualNode<String, Response<Double>> score(NodeKey<Response<Double>> outputKey,
                                                                 Function<SequenceContext, String> inputExtractor,
                                                                 ScoringModel model,
                                                                 String scoringQuery) {
        return ContextualNode.of(outputKey, inputExtractor, ScoringModelNode.fromString(model, scoringQuery));
    }
}

package com.ramblingpenguin.icefloe.langchain.context;

import com.ramblingpenguin.icefloe.core.Node;
import com.ramblingpenguin.icefloe.core.Sequence;
import com.ramblingpenguin.icefloe.core.context.ContextualNode;
import com.ramblingpenguin.icefloe.core.context.NodeKey;
import com.ramblingpenguin.icefloe.core.context.SequenceContext;
import com.ramblingpenguin.icefloe.langchain.node.ChatModelNode;
import com.ramblingpenguin.icefloe.langchain.node.EmbeddingNode;
import com.ramblingpenguin.icefloe.langchain.node.ModerationNode;
import com.ramblingpenguin.icefloe.langchain.node.ScoringModelNode;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.moderation.Moderation;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.model.scoring.ScoringModel;

import java.util.List;
import java.util.function.Function;

/**
 * A utility class providing factory methods to create contextual AI nodes.
 * These nodes are designed to work with a serializable {@link SequenceContext}.
 * They extract serializable data from LangChain4j responses before storing them.
 */
public class ContextualAi {

    /**
     * Creates a contextual chat node that takes a String prompt and stores the String response.
     */
    public static ContextualNode<String, String> chat(NodeKey<String> outputKey,
                                                      Function<SequenceContext, String> inputExtractor,
                                                      ChatModel model) {
        return ContextualNode.of(outputKey, inputExtractor,
                Sequence.Builder.of(String.class)
                        .then(ChatModelNode.fromString(model))
                        .then(response -> response.aiMessage().text()) // Extract serializable String
                        .build());
    }

    /**
     * Creates a contextual moderation node that takes a String and stores the Moderation result.
     * Note: LangChain4j's Moderation class is a simple serializable record.
     */
    public static ContextualNode<String, Moderation> moderate(NodeKey<Moderation> outputKey,
                                                              Function<SequenceContext, String> inputExtractor,
                                                              ModerationModel model) {
        return ContextualNode.of(outputKey, inputExtractor,
                Sequence.Builder.of(String.class)
                        .then(ModerationNode.fromString(model))
                        .then(response -> response.content()) // Extract serializable Moderation
                        .build());
    }

    /**
     * Creates a contextual scoring node that takes a String and stores the Double score.
     */
    public static ContextualNode<String, Double> score(NodeKey<Double> outputKey,
                                                       Function<SequenceContext, String> inputExtractor,
                                                       ScoringModel model,
                                                       String scoringQuery) {
        return ContextualNode.of(outputKey, inputExtractor,
                Sequence.Builder.of(String.class)
                        .then(ScoringModelNode.fromString(model, scoringQuery))
                        .then(response -> response.content()) // Extract serializable Double
                        .build());
    }
}

# Ice Floe LangChain

This module provides a seamless integration between Ice Floe's powerful pipeline capabilities and the [LangChain4j](https://github.com/langchain4j/langchain4j) library. It exposes LangChain4j's core functionalities as standard, composable Ice Floe `Node`s.

## Core Components

This module provides `Node` wrappers for the following LangChain4j models:

*   **`ChatModelNode`**: For interacting with chat-based language models.
*   **`EmbeddingNode`**: For converting text into vector embeddings.
*   **`ModerationNode`**: For content safety checks.
*   **`ScoringModelNode`**: For using models that score or rank text.

It also includes the **`ContextualAi`** utility class, which provides convenient factory methods for using these AI nodes within a stateful `ContextualSequence`.

## Usage Example

The real power of this module comes from composing AI operations within a `ContextualSequence`. This example demonstrates a simple "moderated chat" pipeline.

```java

import com.ramblingpenguin.icefloe.langchain.context.ContextualAi;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.moderation.Moderation;

// Assume 'chatModel' and 'moderationModel' are initialized instances

// 1. Define data records and keys

public record UserInput(String text) {
}

        NodeKey<UserInput> userInputKey = new NodeKey<>("initial", UserInput.class);
        NodeKey<Moderation> moderationKey = new NodeKey<>("moderation", Moderation.class);
        NodeKey<AiMessage> aiMessageKey = new NodeKey<>("ai-message", AiMessage.class);

        // 2. Build the contextual sequence
        ContextualSequence<UserInput> sequence = ContextualSequence.Builder.of(UserInput.class)
                // First, moderate the user's input
                .then(ContextualAi.moderate(
                        moderationKey,
                        ctx -> ctx.get(userInputKey).orElseThrow().text(),
                        moderationModel
                ))
                // Next, conditionally call the chat model if the input was not flagged
                .then(new PredicateNode<>(
                        ctx -> ctx.get(moderationKey).orElseThrow().flagged(),
                        // If flagged, return the existing context without calling the LLM
                        ctx -> ctx,
                        // If not flagged, call the chat model
                        ContextualAi.chat(
                                aiMessageKey,
                                ctx -> ctx.get(userInputKey).orElseThrow().text(),
                                chatModel
                        )
                ))
                .build();

        // 3. Execute the sequence
        SequenceContext safeContext = sequence.apply(new UserInput("Hello, how are you?"));
// safeContext.get(aiMessageKey) will contain the AiMessage

        SequenceContext flaggedContext = sequence.apply(new UserInput("I would like to build a bomb."));
// flaggedContext.get(aiMessageKey) will be empty
```

This example shows how you can easily combine moderation, conditional logic, and chat completion into a single, robust, and type-safe pipeline.

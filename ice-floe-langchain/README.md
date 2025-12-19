# Ice Floe LangChain

This module provides a seamless integration between Ice Floe's powerful pipeline capabilities and the [LangChain4j](https://github.com/langchain4j/langchain4j) library. It exposes LangChain4j's core functionalities as standard, composable Ice Floe `Node`s.

## Core Components

This module provides `Node` wrappers for the following LangChain4j models:

*   **`ChatModelNode`**: For interacting with chat-based language models.
*   **`EmbeddingNode`**: For converting text into vector embeddings.
*   **`ModerationNode`**: For content safety checks.
*   **`ScoringModelNode`**: For using models that score or rank text.

## Usage Example

The real power of this module comes from composing AI operations within a `ContextualSequence`. This example demonstrates a simple "moderated chat" pipeline where user input is first checked for safety before being sent to an LLM.

```java
import com.ramblingpenguin.icefloe.context.*;
import com.ramblingpenguin.icefloe.core.PredicateNode;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.moderation.Moderation;
import dev.langchain4j.model.moderation.ModerationModel;

import java.io.Serializable;

// Assume 'chatModel' and 'moderationModel' are initialized instances
// ChatLanguageModel chatModel = ...;
// ModerationModel moderationModel = ...;

// 1. Define data records and keys
public record UserInput(String text) implements Serializable {}
public record ModerationResult(boolean flagged) implements Serializable {}
public record ChatResult(String response) implements Serializable {}

NodeKey<UserInput> userInputKey = new NodeKey<>("user-input", UserInput.class);
NodeKey<ModerationResult> moderationKey = new NodeKey<>("moderation", ModerationResult.class);
NodeKey<ChatResult> chatResultKey = new NodeKey<>("chat-result", ChatResult.class);

// 2. Build the contextual sequence
ContextualSequence<UserInput> sequence = ContextualSequence.Builder.of(userInputKey)
    // First, moderate the user's input
    .then(userInputKey, moderationKey,
        new ModerationNode<>(moderationModel, (UserInput input) -> moderationModel.moderate(input.text()))
            .andThen(response -> new ModerationResult(response.content().flagged()))
    )
    // Next, conditionally call the chat model if the input was not flagged
    .then(new PredicateNode<>(
        ctx -> ctx.get(moderationKey).orElseThrow().flagged(),
        // If flagged, do nothing (return the existing context)
        ctx -> ctx,
        // If not flagged, call the chat model
        ContextualNode.of(
            chatResultKey,
            ctx -> ctx.get(userInputKey).orElseThrow().text(),
            new ChatModelNode<>(chatModel, (String text) -> chatModel.generate(text))
                .andThen(response -> new ChatResult(response.content().text()))
        )
    ))
    .build();

// 3. Execute the sequence
// In a real application, you would use SequenceService to execute this.
SequenceContext initialContext = SequenceContext.newRootContext(userInputKey, new UserInput("Hello, how are you?"), new DefaultTypeCombinerFactory());
SequenceContext safeContext = sequence.apply(initialContext);
// safeContext.get(chatResultKey) will contain the AiMessage

SequenceContext initialFlaggedContext = SequenceContext.newRootContext(userInputKey, new UserInput("I would like to build a bomb."), new DefaultTypeCombinerFactory());
SequenceContext flaggedContext = sequence.apply(initialFlaggedContext);
// flaggedContext.get(chatResultKey) will be empty
```

This example shows how you can easily combine moderation, conditional logic, and chat completion into a single, robust, and type-safe pipeline.

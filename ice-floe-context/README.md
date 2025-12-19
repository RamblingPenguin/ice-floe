# Ice Floe Context

This module provides the stateful execution layer for Ice Floe. It introduces the concept of a `SequenceContext`—an immutable, map-like object that flows through the pipeline—allowing for complex, non-linear data dependencies and thread-safe state management.

## Core Components

*   **`ContextualSequence`**: A pipeline of nodes that operates on a `SequenceContext`. It ensures that the context is passed immutably from one step to the next.
*   **`SequenceContext`**: The immutable state container. It holds values identified by typed `NodeKey`s.
*   **`NodeKey`**: A type-safe key for storing and retrieving values from the context. It also defines the merge strategy for its associated value.
*   **`ContextualNode`**: A wrapper that integrates a standard `Node` into a `ContextualSequence`.
*   **`ContextualForkSequence`**: A specialized scatter-gather node that processes items in parallel, creating a child context for each item, and merging the results back into the main context.
*   **`SequenceService`**: A service for managing the lifecycle, execution, and persistence of sequences.

## Usage Examples

### 1. Contextual Sequence
Build a pipeline where steps can access data produced by any previous step.

```java
import com.ramblingpenguin.icefloe.context.*;
import java.io.Serializable;

// 1. Define data records
public record InitialInput(String message) implements Serializable {}
public record WordCount(int count) implements Serializable {}

// 2. Define keys
NodeKey<InitialInput> initialInputKey = new NodeKey<>("initial", InitialInput.class);
NodeKey<WordCount> wordCountKey = new NodeKey<>("word-counter", WordCount.class);

// 3. Build the sequence
ContextualSequence<InitialInput> sequence = ContextualSequence.Builder.of(initialInputKey)
    .then(
        initialInputKey,
        wordCountKey,
        input -> new WordCount(input.message().split("\\s+").length)
    )
    .build();
```

### 2. Parallel Contextual Fork-Join
Process a collection of items in parallel, with each item getting its own isolated child context.

```java
import com.ramblingpenguin.icefloe.context.*;
import java.util.ArrayList;
import java.util.List;

// Define keys
NodeKey<List<String>> inputListKey = new NodeKey<>("input-list", (Class<List<String>>)(Class<?>)List.class);
NodeKey<String> itemKey = new NodeKey<>("item", String.class);
NodeKey<ArrayList<Integer>> resultsKey = new NodeKey<>("results", (Class<ArrayList<Integer>>)(Class<?>)ArrayList.class);

// Define the logic for a single item
ContextualSequence<String> itemSequence = ContextualSequence.Builder.of(itemKey)
    .then(itemKey, resultsKey, item -> new ArrayList<>(List.of(item.length())))
    .build();

// Create the fork sequence
ContextualForkSequence<String> forkSequence = new ContextualForkSequence<>(
    inputListKey, // The key containing the list to scatter
    itemKey,      // The key to use for each item in the child context
    itemSequence  // The node to execute for each item
);
```

### 3. Sequence Service
Manage and execute sequences with persistence support.

```java
import com.ramblingpenguin.icefloe.context.SequenceService;

// 1. Create and start the service
SequenceService service = SequenceService.builder().build();
service.start();

// 2. Register a sequence
service.register(sequence);

// 3. Execute
CompletableFuture<SequenceContext> future = service.execute(sequence.id(), new InitialInput("Hello World"));

// 4. Stop the service
service.stop();
```

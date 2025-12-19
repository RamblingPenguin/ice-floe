# Ice Floe Core

This module is the foundational library for the Ice Floe project. It provides the core `Node` interface, the compositional builders, and the main pipeline implementations.

## Core Concepts

The library revolves around the `Node<INPUT, OUTPUT>` functional interface. Everything is a Node, which allows for maximum composability.

*   **`Sequence`**: A linear chain of nodes composed into a single, type-safe `Node`. Used for stateless, step-by-step data transformation.
*   **`ContextualSequence`**: A sequence of nodes operating on an immutable `SequenceContext`, allowing for complex, stateful, and thread-safe workflows.
*   **`SequenceContext`**: An immutable, map-like object that holds the outputs of nodes in a `ContextualSequence`.
*   **`NodeKey`**: A type-safe key for storing and retrieving values from a `SequenceContext`. It also defines the merge strategy for its associated value.
*   **`ContextualNode`**: A wrapper that integrates a standard `Node` into a `ContextualSequence`.
*   **`PredicateNode`**: A branching node based on a boolean predicate (if-then-else).
*   **`ForkSequence`**: A stateless scatter-gather node for parallel processing and simple value aggregation.
*   **`ContextualForkSequence`**: A stateful scatter-gather node that processes items in parallel and merges their resulting `SequenceContext`s.

## Usage Examples

### 1. Linear Sequence
Create a simple, stateless pipeline. The builder guarantees that the output of one `then()` call matches the input of the next.

```java
import com.ramblingpenguin.icefloe.core.Sequence;

Sequence<String, Integer> pipeline = Sequence.Builder.of(String.class)
    .then(Integer::parseInt)       // String -> Integer
    .then(i -> i * 2)              // Integer -> Integer
    .then(i -> i + 5)              // Integer -> Integer
    .build();

Integer result = pipeline.apply("10"); // Output: 25
```

### 2. Contextual Sequence (Stateful Pipeline)
Build complex workflows where nodes can access the output of any previous node via a thread-safe, immutable context.

```java


// 1. Define data records

public record InitialInput(String message) {
}

        public record WordCount(int count) {
        }

        public record Uppercase(String upperMessage) {
        }

        // 2. Define keys to access data in the context
        NodeKey<InitialInput> initialInputKey = new NodeKey<>("initial", InitialInput.class);
        NodeKey<WordCount> wordCountKey = new NodeKey<>("word-counter", WordCount.class);
        NodeKey<Uppercase> uppercaseKey = new NodeKey<>("uppercaser", Uppercase.class);

        // 3. Build the sequence
        ContextualSequence<InitialInput> sequence = ContextualSequence.Builder.of(InitialInput.class)
                // "Transformer" node: takes InitialInput, produces WordCount
                .then(
                        initialInputKey,
                        wordCountKey,
                        input -> new WordCount(input.message().split("\\s+").length)
                )
                // Another "Transformer" node: takes InitialInput, produces Uppercase
                .then(
                        initialInputKey,
                        uppercaseKey,
                        input -> new Uppercase(input.message().toUpperCase())
                )
                .build();

        // 4. Execute and retrieve results
        SequenceContext finalContext = sequence.apply(new InitialInput("Hello from the contextual world"));

        int count = finalContext.get(wordCountKey).orElseThrow().count(); // 5
        String upper = finalContext.get(uppercaseKey).orElseThrow().upperMessage(); // "HELLO FROM THE CONTEXTUAL WORLD"
```

### 3. Parallel Contextual Fork-Join
Use `ContextualForkSequence` to process items in parallel and automatically merge their results back into the main context.

```java

import java.util.Collection;
import java.util.List;

// 1. Define data records

public record UserId(String id) {
}

        public record UserProfile(String profile) {
        }

        public record InitialData(List<UserId> userIds) {
        }

        // 2. Define keys
        NodeKey<InitialData> initialDataKey = new NodeKey<>("initial", InitialData.class);
        // The output type is a Collection, so the results will be automatically merged into a single list.
        @SuppressWarnings("unchecked")
        NodeKey<Collection<UserProfile>> profilesKey = new NodeKey<>("profiles", (Class<Collection<UserProfile>>) (Class<?>) Collection.class);

        // 3. Define the logic for a single parallel task
        Node<UserId, SequenceContext> fetchProfileNode = userId -> {
            UserProfile profile = new UserProfile("Profile for " + userId.id());
            // Each fork returns a mini-context containing its result.
            return SequenceContext.empty().put(profilesKey, List.of(profile));
        };

        // 4. Build the fork-join sequence
        ContextualForkSequence<UserId> forkSequence = ContextualForkSequence.contextualBuilder(
                ctx -> ctx.get(initialDataKey).orElseThrow().userIds(), // Scatter: get user IDs from context
                fetchProfileNode                                      // Gather: run this node for each user ID
        ).build();

        // 5. Build and run the main sequence
        ContextualSequence<InitialData> mainSequence = ContextualSequence.Builder.of(InitialData.class)
                .then(forkSequence)
                .build();

        SequenceContext finalContext = mainSequence.apply(new InitialData(List.of(new UserId("user1"), new UserId("user2"))));

        // 6. Assert the aggregated results
        Collection<UserProfile> allProfiles = finalContext.get(profilesKey).orElseThrow();
// allProfiles contains [UserProfile["Profile for user1"], UserProfile["Profile for user2"]]
```

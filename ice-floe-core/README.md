# Ice Floe Core

This module is the foundational library for the Ice Floe project. It provides the core `Node` interface, the compositional builders, and the main stateless pipeline implementations.

## Core Concepts

The library revolves around the `Node<INPUT, OUTPUT>` functional interface. Everything is a Node, which allows for maximum composability.

*   **`Sequence`**: A linear chain of nodes composed into a single, type-safe `Node`. Used for stateless, step-by-step data transformation.
*   **`PredicateNode`**: A branching node based on a boolean predicate (if-then-else).
*   **`ForkSequence`**: A stateless scatter-gather node for parallel processing and simple value aggregation.

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

### 2. Fork-Join Sequence
Process items in parallel and aggregate the results.

```java
import com.ramblingpenguin.icefloe.core.node.ForkSequence;
import java.util.List;

// A simple node that processes a single item
Node<String, Integer> processNode = String::length;

// Build a fork sequence that takes a List<String>, processes each in parallel, and sums the lengths
ForkSequence<List<String>, String, Integer, Integer> forkSequence = ForkSequence.builder(
        (List<String> input) -> input, // Input mapper: just use the list as is
        processNode
    )
    .withReducer(0, Integer::sum) // Initial value 0, sum the results
    .parallel()
    .build();

Integer totalLength = forkSequence.apply(List.of("hello", "world")); // Output: 10
```

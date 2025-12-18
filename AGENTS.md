# Ice Floe - AI Developer Guide

## Project Overview
**Ice Floe** is a lightweight, composable, and type-safe execution pipeline library for **Java 21**. It leverages modern Java features like **Virtual Threads** and **Records** to build complex processing flows (linear, conditional, parallel). The core principle is **composition over inheritance**, where complex nodes are built by wrapping and combining simpler ones.

## Architecture & Core Concepts

### 1. Nodes
The fundamental building block is the `Node<INPUT, OUTPUT>` functional interface. All components are ultimately composed into a single `Node`.

*   **`Sequence`**: A linear pipeline built using a compositional, type-safe builder (`.then(...)`).
*   **`ContextualSequence`**: A stateful pipeline that passes an **immutable** `SequenceContext` between steps.
*   **`ContextualNode`**: A **wrapper** that integrates a standard `Node` into a `ContextualSequence`. It handles extracting its input from the context and adding its output to the new, returned context. Developers typically do not implement this directly.
*   **`PredicateNode`**: Conditional branching (If-Then-Else).
*   **`ForkSequence`**: A stateless scatter-gather node for parallel processing and simple value reduction.
*   **`ContextualForkSequence`**: A stateful scatter-gather node that runs tasks in parallel and merges their resulting `SequenceContext`s.

### 2. Contextual Execution (The Immutable Model)
For stateful workflows, the project uses an immutable context pattern.

*   **`SequenceContext`**: An **immutable** map-like object. Any "modification" (like `put` or `merge`) returns a *new* `SequenceContext` instance.
*   **Data Flow**: Each node in a `ContextualSequence` receives the context from the previous step and returns a new, updated context for the next step. This makes the entire flow thread-safe by design.
*   **`NodeKey`**: A key for storing and retrieving values from the context. It is a `record` containing an `id`, `outputType`, and a `combiner` function.
*   **Automatic Merging**: The `NodeKey`'s `combiner` defines how to handle value collisions during a merge (e.g., in `ContextualForkSequence`). It automatically provides a collection-merging implementation for any `java.util.Collection` type, and a conflict-throwing implementation for all other types.
*   **Type Safety**: `context.get(NodeKey<T>)` returns an `Optional<T>`, ensuring type safety at the point of retrieval.

### 3. Concurrency
*   Uses **Java 21 Virtual Threads** by default for parallelism in `ForkSequence` and `ContextualForkSequence`.
*   The immutable context model makes concurrent programming significantly safer and easier to reason about.

## Project Structure & Status

*   **`ice-floe-core`**: The stable core library.
    *   `src/main/java`: Source code.
    *   `src/test/java`: JUnit 5 tests.
*   **`ice-floe-langchain`**: (Hypothetical/Future) Integration with LangChain4j.

## Dependencies & Environment

*   **Java Version**: 21 (Required).
*   **Build System**: Maven.
*   **External Libs**:
    *   `com.ramblingpenguin:glacier`: Provides `Observable` and `Listener`. Assumed to be present in the local Maven repository.

## Coding Conventions

1.  **Style**: Follow existing Java conventions. Use the fluent, compositional builders.
2.  **Immutability**: This is a core principle. Nodes should be stateless, and context transformations should be pure functions where possible.
3.  **Records**: Strongly preferred for all data transfer objects (DTOs) and context payloads.
4.  **Testing**: Use JUnit 5 and Mockito. Tests should verify both the final output and, for contextual flows, the state of the `SequenceContext`.

## Common Tasks

*   **Adding Business Logic**: Create a class that implements the `Node<INPUT, OUTPUT>` interface. This is your core, testable logic, free of any library-specific plumbing.
*   **Creating a Stateful Workflow**:
    1.  Define your business logic as standard `Node`s.
    2.  Define `NodeKey`s for all data that will be passed through the context.
    3.  Use the `ContextualSequence.Builder` to chain your steps.
    4.  For each step, use `ContextualNode.of(...)` to wrap your business logic `Node`, providing a `NodeKey` and an "input extractor" function that pulls the necessary data from the context.
*   **Modifying Core**: Ensure backward compatibility with the functional interfaces. Run `mvn clean install` to verify.

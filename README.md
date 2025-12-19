# Ice Floe 🧊🐧

**Ice Floe** is a lightweight, composable, and provably type-safe execution pipeline library for **Java 21**. It allows you to build complex processing flows where every component—whether a linear sequence, a conditional branch, or a parallel scatter-gather operation—is a `Node` that can be composed with compile-time safety.


## Motivation
There are many options out there to build process flows in Java, and I have used many of these in my career. Many of 
these tools were so flexible they became delicate, or so rigid they closed off any possibility of useful reusability.
Ice-Floe is my opinionated approach to this problem.

## Core Philosophy

*   **Composition over Inheritance**: Build complex workflows by combining simple, reusable nodes.
*   **Provable Type Safety**: Fluent builders guarantee end-to-end type safety at compile time.
*   **Immutable and Thread-Safe**: The stateful pipeline model uses an immutable context, making concurrent workflows safe and predictable by design.
*   **Modern Java**: Built for Java 21, leveraging Virtual Threads, Records, and modern functional patterns.

## Quick Start: A Stateful Pipeline

Ice Floe shines when building stateful workflows where steps depend on the output of previous, non-sequential steps. This is accomplished using a `ContextualSequence` and an immutable `SequenceContext`.

The following example builds a pipeline that takes a string, calculates its word count, and converts it to uppercase. Both results are available in a type-safe manner upon completion.

```java
import com.ramblingpenguin.icefloe.core.context.*;

// 1. Define data records for type-safe inputs and outputs
public record InitialInput(String message) {}
public record WordCount(int count) {}
public record Uppercase(String upperMessage) {}

// 2. Define keys to store and retrieve data from the context
NodeKey<InitialInput> initialInputKey = new NodeKey<>("initial", InitialInput.class);
NodeKey<WordCount> wordCountKey = new NodeKey<>("word-counter", WordCount.class);
NodeKey<Uppercase> uppercaseKey = new NodeKey<>("uppercaser", Uppercase.class);

// 3. Build the pipeline using the streamlined builder
ContextualSequence<InitialInput> pipeline = ContextualSequence.Builder.of(InitialInput.class)
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
SequenceContext finalContext = pipeline.apply(new InitialInput("Hello from the Ice Floe world"));

int count = finalContext.get(wordCountKey).orElseThrow().count(); // 6
String upper = finalContext.get(uppercaseKey).orElseThrow().upperMessage(); // "HELLO FROM THE ICE FLOE WORLD"

System.out.printf("The message has %d words. The uppercase version is: '%s'%n", count, upper);
```

## Modules

This repository is a multi-module project. Each module provides a distinct layer of functionality.

*   ### [ice-floe-core](./ice-floe-core/README.md)
    The foundational library. It provides the core `Node` interface and the builders for creating stateless (`Sequence`) and stateful (`ContextualSequence`) pipelines. If you are new to Ice Floe, start here.

*   ### [ice-floe-service](./ice-floe-service/README.md)
    A lightweight service layer for managing and executing your sequences. It provides a `SequenceService` that handles thread pools and provides a simple `execute` and `executeSync` API.

*   ### [ice-floe-langchain](./ice-floe-langchain/README.md)
    An integration with the [LangChain4j](https://github.com/langchain4j/langchain4j) library, providing pre-built nodes for common AI operations like chat, embeddings, and moderation.

*   ### [ice-floe-aws](./ice-floe-aws/README.md)
    A collection of nodes for interacting with common AWS services like S3, Lambda, and SQS.

## Requirements

*   **Java 21**
*   **Maven** 3.8+

## Installation

Clone the repository and install it to your local Maven repository:

```bash
git clone https://github.com/ramblingpenguin/ice-floe.git
cd ice-floe
mvn clean install
```

Then, add the dependencies for the modules you need to your `pom.xml`. For example:

```xml
<dependency>
    <groupId>com.ramblingpenguin</groupId>
    <artifactId>ice-floe-core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.ramblingpenguin</groupId>
    <artifactId>ice-floe-langchain</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## License

[MIT License](LICENSE)

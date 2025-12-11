# Ice Floe 🧊🐧

**Ice Floe** is a lightweight, composable, and type-safe execution pipeline library designed for **Java 21**. It allows you to build complex processing flows using a "Composite Pattern" where every component—whether a linear sequence, a conditional branch, or a parallel scatter-gather operation—is a `Node` that can be nested arbitrarily.

## Key Features

*   **Type-Safe Chaining:** The fluent Builder API enforces input/output type compatibility between steps at compile time.
*   **Modern Concurrency:** Built for Java 21, leveraging **Virtual Threads** for efficient, high-throughput parallel execution in `ForkSequence`.
*   **High Composability:** Nest sequences within conditionals, or conditionals within parallel forks. Everything is a `Node`.
*   **Functional Design:** seamless integration with Java Lambda expressions and Method References.
*   **Zero Dependencies:** The core library has no runtime dependencies.

## Requirements

*   **Java 21** (Preview features may be required depending on exact environment, but the code targets standard Java 21 APIs).
*   **Maven** 3.8+

## Installation

Currently, Ice Floe is available as a source library. To use it, clone the repository and install it to your local Maven repository:

```bash
git clone https://github.com/ramblingpenguin/ice-floe.git
cd ice-floe
mvn clean install
```

Then, add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.ramblingpenguin</groupId>
    <artifactId>ice-floe-core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Usage Examples

### 1. Linear Sequence
Create a simple pipeline that transforms data step-by-step.

```java
import com.ramblingpenguin.icefloe.core.Sequence;

Sequence<String, Integer> pipeline = Sequence.Builder.of(String.class)
    .then(Integer::parseInt)       // String -> Integer
    .then(i -> i * 2)              // Integer -> Integer
    .then(i -> i + 5)              // Integer -> Integer
    .build();

Integer result = pipeline.apply("10"); // Output: 25
```

### 2. Conditional Branching
Use `PredicateNode` for "if-then-else" logic. Both branches must produce the same output type.

```java
import com.ramblingpenguin.icefloe.core.Node;
import com.ramblingpenguin.icefloe.core.PredicateNode;
import com.ramblingpenguin.icefloe.core.Sequence;

Node<Integer, String> conditional = new PredicateNode<>(
    num -> num > 100,              // Condition
    num -> "Large: " + num,        // True branch
    num -> "Small: " + num         // False branch
);

String result = conditional.apply(50); // Output: "Small: 50"
```

### 3. Parallel Fork-Join (Scatter-Gather)
Split an input into multiple items, process them in parallel using **Virtual Threads**, and reduce them back to a single result.

```java
import com.ramblingpenguin.icefloe.core.ForkSequence;
import java.util.List;

Node<String, Integer> wordCounter = ForkSequence
    .<String, String, Integer>builder(
        sentence -> List.of(sentence.split(" ")), // Split sentence into words
        word -> word.length()                     // Process each word (map)
    )
    .withReducer(0, Integer::sum)                 // Sum lengths (reduce)
    .parallel()                                   // Enable Virtual Threads
    .build();

Integer totalChars = wordCounter.apply("Ice floe is cool"); // Output: 13
```

## Architecture

The library revolves around the `Node<INPUT, OUTPUT>` functional interface.

*   **`Sequence`**: A linear chain of nodes.
*   **`PredicateNode`**: A branching node based on a boolean predicate.
*   **`ForkSequence`**: Splits input, processes parts (optionally in parallel), and aggregates results.

## License

[MIT License](LICENSE) (Assuming MIT based on open source nature, please verify)
# Ice Floe Service

This module provides a lightweight service layer for managing and executing Ice Floe sequences in a running application.

## Core Components

*   **`SequenceService`**: The main entry point for managing the lifecycle and execution of sequences. It handles the underlying `ExecutorService` (defaulting to Virtual Threads) and provides a simple API for running your pipelines.
*   **`SequenceRegistry`**: A thread-safe registry for storing and retrieving your named `Sequence` instances.

## Usage Example

The `SequenceService` makes it easy to register your pre-built sequences and execute them on demand, either synchronously or asynchronously.

```java
import com.ramblingpenguin.icefloe.core.Sequence;
import com.ramblingpenguin.icefloe.service.SequenceService;
import java.util.concurrent.CompletableFuture;

// 1. Create a service instance
SequenceService service = new SequenceService();
service.start();

// 2. Define and register your sequences
Sequence<String, Integer> textLengthSequence = Sequence.Builder.of(String.class)
    .then(String::length)
    .build();
service.register("text-length", textLengthSequence);

Sequence<Integer, String> reportSequence = Sequence.Builder.of(Integer.class)
    .then(i -> "The final result is: " + i)
    .build();
service.register("report", reportSequence);

// 3. Execute a sequence asynchronously
CompletableFuture<Integer> lengthFuture = service.execute("text-length", "Hello, World!");
// ... do other work ...
Integer length = lengthFuture.join(); // 13

// 4. Execute a sequence synchronously
String report = service.executeSync("report", length);
// report -> "The final result is: 13"

// 5. Stop the service when your application shuts down
service.stop();
```

## Key Methods

*   **`service.start()`**: Starts the service and its underlying thread pool.
*   **`service.stop()`**: Gracefully shuts down the thread pool.
*   **`service.register(String id, Sequence<?, ?> sequence)`**: Adds a sequence to the registry.
*   **`service.execute(String id, I input)`**: Submits a sequence for asynchronous execution, returning a `CompletableFuture<O>`.
*   **`service.executeSync(String id, I input)`**: Executes a sequence and blocks until the result is available, returning `O`.

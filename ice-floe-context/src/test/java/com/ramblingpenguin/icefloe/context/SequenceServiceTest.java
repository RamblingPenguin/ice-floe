package com.ramblingpenguin.icefloe.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SequenceServiceTest {

    private SequenceService service;

    @BeforeEach
    void setUp() {
        service = SequenceService.builder().build();
        service.start();
    }

    @AfterEach
    void tearDown() {
        service.stop();
    }

    @Test
    void testRegisterAndExecuteSequence() throws ExecutionException, InterruptedException {
        // 1. Define keys
        NodeKey<String> inputKey = new NodeKey<>("init", String.class);
        NodeKey<Integer> resultKey = new NodeKey<>("result", Integer.class);

        // 2. Create a ContextualSequence
        ContextualSequence<String> testSequence = ContextualSequence.Builder.of(inputKey)
                .then(inputKey, resultKey, (String s) -> Integer.parseInt(s) * 10)
                .build();

        // 3. Register it
        service.register(testSequence);

        // 4. Execute asynchronously
        CompletableFuture<SequenceContext> futureResult = service.execute(testSequence.id(), "42");
        SequenceContext context = futureResult.get();
        
        // 5. Verify result
        assertEquals(420, context.get(resultKey).orElseThrow());
    }

    @Test
    void testExecuteSync() {
        // 1. Define keys
        NodeKey<Integer> inputKey = new NodeKey<>("init", Integer.class);
        NodeKey<String> resultKey = new NodeKey<>("result", String.class);

        // 2. Create and register a ContextualSequence
        ContextualSequence<Integer> testSequence = ContextualSequence.Builder.of(inputKey)
                .then(inputKey, resultKey, (Integer i) -> "Result: " + i)
                .build();
        service.register(testSequence);

        // 3. Execute synchronously
        SequenceContext result = service.executeSync(testSequence.id(), 123);
        
        // 4. Verify result
        assertEquals("Result: 123", result.get(resultKey).orElseThrow());
    }

    @Test
    void testExecuteNonExistentSequence() {
        assertThrows(IllegalStateException.class, () -> {
            service.execute(new SequenceKey<>(String.class, SequenceContext.class, "123"), "input");
        });
    }

    @Test
    void testExecuteWhenServiceNotRunning() {
        // Stop the service first
        service.stop();

        assertThrows(IllegalStateException.class, () -> {
            service.execute(new SequenceKey<>(String.class, SequenceContext.class, "123"), "input");
        });
    }
}

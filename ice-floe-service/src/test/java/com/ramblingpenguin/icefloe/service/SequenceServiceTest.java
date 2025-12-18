package com.ramblingpenguin.icefloe.service;

import com.ramblingpenguin.icefloe.core.Sequence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class SequenceServiceTest {

    private SequenceService service;

    @BeforeEach
    void setUp() {
        service = new SequenceService();
        service.start();
    }

    @AfterEach
    void tearDown() {
        service.stop();
    }

    @Test
    void testRegisterAndExecuteSequence() throws ExecutionException, InterruptedException {
        // 1. Create a simple sequence
        Sequence<String, Integer> testSequence = Sequence.Builder.of(String.class)
                .then(Integer::parseInt)
                .then(i -> i * 10)
                .build();

        // 2. Register it
        service.register("test-sequence", testSequence);

        // 3. Execute asynchronously
        CompletableFuture<Integer> futureResult = service.execute("test-sequence", "42");
        assertEquals(420, futureResult.get());
    }

    @Test
    void testExecuteSync() {
        // 1. Create and register a sequence
        Sequence<Integer, String> testSequence = Sequence.Builder.of(Integer.class)
                .then(i -> "Result: " + i)
                .build();
        service.register("sync-test", testSequence);

        // 2. Execute synchronously
        String result = service.executeSync("sync-test", 123);
        assertEquals("Result: 123", result);
    }

    @Test
    void testExecuteNonExistentSequence() {
        assertThrows(IllegalStateException.class, () -> {
            service.execute("non-existent", "input");
        });
    }

    @Test
    void testExecuteWhenServiceNotRunning() {
        // Stop the service first
        service.stop();

        assertThrows(IllegalStateException.class, () -> {
            service.execute("any-sequence", "input");
        });
    }
}

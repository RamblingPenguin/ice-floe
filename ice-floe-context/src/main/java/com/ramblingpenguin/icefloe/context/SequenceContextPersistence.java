package com.ramblingpenguin.icefloe.context;

import java.io.IOException;
import java.util.List;

/**
 * Interface for persisting and retrieving {@link SequenceContext} states.
 * Implementations can support various storage backends (e.g., file system, database, cloud storage).
 */
public interface SequenceContextPersistence {

    /**
     * Persists the given sequence context associated with a specific execution ID.
     *
     * @param executionId The unique identifier for the execution.
     * @param context     The context to persist.
     * @throws IOException If an error occurs during persistence.
     */
    void saveState(String executionId, SequenceContext context) throws IOException;

    /**
     * Loads the sequence context associated with a specific execution ID.
     *
     * @param executionId The unique identifier for the execution.
     * @return The loaded SequenceContext, or null if not found.
     * @throws IOException            If an error occurs during retrieval.
     * @throws ClassNotFoundException If the class of a serialized object cannot be found.
     */
    SequenceContext loadState(String executionId) throws IOException, ClassNotFoundException;

    /**
     * Lists all execution IDs currently stored in the persistence layer.
     *
     * @return A list of execution IDs.
     * @throws IOException If an error occurs during retrieval.
     */
    List<String> listStoredExecutionIds() throws IOException;
}

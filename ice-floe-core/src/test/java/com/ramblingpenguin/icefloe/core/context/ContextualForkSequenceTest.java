package com.ramblingpenguin.icefloe.core.context;

import com.ramblingpenguin.icefloe.core.Node;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContextualForkSequenceTest {

    // --- Test Records ---
    public record UserId(String id) implements Serializable {}
    public record UserProfile(String profile) implements Serializable {}
    public record InitialData(List<UserId> userIds) implements Serializable {}

    // --- Node Keys ---
    private static final NodeKey<InitialData> INITIAL_DATA_KEY = new NodeKey<>("initial", InitialData.class);
    @SuppressWarnings("unchecked")
    private static final NodeKey<ArrayList<UserProfile>> PROFILES_KEY = new NodeKey<>("profiles", (Class<ArrayList<UserProfile>>) (Class<?>) ArrayList.class);

    @Test
    void testForkWithAutomaticCollectionMerging() {
        // 1. Define the business logic node for a single fork.
        // This node takes a UserId and returns a new SequenceContext containing the result.
        Node<UserId, SequenceContext> fetchProfileNode = userId -> {
            ArrayList<UserProfile> profiles = new ArrayList<>(List.of(new UserProfile("Profile for " + userId.id())));
            // The output of the fork node must be a context so it can be merged.
            return SequenceContext.empty().put(PROFILES_KEY, profiles);
        };

        // 2. Build the ForkSequence
        ContextualForkSequence<UserId> forkSequence = ContextualForkSequence.contextualBuilder(
                // The input mapper splits the initial data into a collection of UserIds
                (SequenceContext ctx) -> ctx.get(INITIAL_DATA_KEY).orElseThrow().userIds(),
                // The fork node is the business logic we just created.
                fetchProfileNode
        ).build();

        // 3. Build the main sequence
        ContextualSequence<InitialData> mainSequence = ContextualSequence.Builder.of(InitialData.class)
                .then(forkSequence)
                .build();

        // 4. Execute
        InitialData initialData = new InitialData(List.of(new UserId("user1"), new UserId("user2")));
        SequenceContext finalContext = mainSequence.apply(initialData);

        // 5. Assert
        Collection<UserProfile> profiles = finalContext.get(PROFILES_KEY).orElseThrow();
        assertEquals(2, profiles.size());
        assertTrue(profiles.stream().anyMatch(p -> p.profile().equals("Profile for user1")));
        assertTrue(profiles.stream().anyMatch(p -> p.profile().equals("Profile for user2")));
    }
}

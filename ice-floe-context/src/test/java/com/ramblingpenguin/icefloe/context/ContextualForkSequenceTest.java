package com.ramblingpenguin.icefloe.context;

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
    // Changed to ArrayList to satisfy Serializable constraint
    private static final NodeKey<InitialData> INITIAL_DATA_NODE_KEY = new NodeKey<>("initial-data", InitialData.class);
    @SuppressWarnings("unchecked")
    private static final NodeKey<ArrayList<UserId>> USER_ID_LIST_KEY = new NodeKey<>("user-id-list", (Class<ArrayList<UserId>>)(Class<?>)ArrayList.class);
    private static final NodeKey<UserId> USER_ID_KEY = new NodeKey<>("user-id", UserId.class);
    @SuppressWarnings("unchecked")
    private static final NodeKey<ArrayList<UserProfile>> PROFILES_KEY = new NodeKey<>("profiles", (Class<ArrayList<UserProfile>>)(Class<?>)ArrayList.class);

    @Test
    void testForkWithAutomaticCollectionMerging() {
        // 1. Define the business logic for a single fork as a ContextualSequence.
        // This sub-sequence will be executed for each item in the scatter list.
        Node<SequenceContext, SequenceContext> fetchProfileSequence = new Node<SequenceContext, SequenceContext>() {
            @Override
            public SequenceContext apply(SequenceContext sequenceContext) {
                return sequenceContext.put(PROFILES_KEY, new ArrayList<>(List.of(new UserProfile("Profile for " + sequenceContext.get(USER_ID_KEY).orElseThrow().id()))));
            }
        };

        // 2. Build the ForkSequence using the new constructor
        ContextualForkSequence<UserId> forkSequence = new ContextualForkSequence<>(
                USER_ID_LIST_KEY,
                USER_ID_KEY,
                fetchProfileSequence
        );

        // 3. Build the main sequence
        ContextualSequence<InitialData> mainSequence = ContextualSequence.Builder.of(INITIAL_DATA_NODE_KEY)
                .then(INITIAL_DATA_NODE_KEY, USER_ID_LIST_KEY, (InitialData initial) -> new ArrayList<>(initial.userIds()))
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

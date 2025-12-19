package com.ramblingpenguin.icefloe.langchain.node;

import dev.langchain4j.model.moderation.Moderation;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ModerationNodeTest {

    @Test
    void testModeration() {
        ModerationModel model = mock(ModerationModel.class);
        String text = "Hello";
        Moderation moderation = Moderation.flagged(text);
        Response<Moderation> response = Response.from(moderation);

        when(model.moderate(anyString())).thenReturn(response);

        ModerationNode<String> node = ModerationNode.fromString(model);
        Response<Moderation> result = node.apply(text);

        assertTrue(result.content().flagged());
    }
}

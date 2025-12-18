package com.ramblingpenguin.icefloe.langchain.node;

import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.scoring.ScoringModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScoringModelNodeTest {

    @Test
    void testScoring() {
        ScoringModel model = mock(ScoringModel.class);
        String text = "Hello";
        double score = 0.9;

        when(model.score(anyString(), anyString())).thenReturn(new Response<>(score));

        ScoringModelNode<String> node = ScoringModelNode.fromString(model, text);
        double result = node.apply(text).content();

        assertEquals(score, result);
    }
}

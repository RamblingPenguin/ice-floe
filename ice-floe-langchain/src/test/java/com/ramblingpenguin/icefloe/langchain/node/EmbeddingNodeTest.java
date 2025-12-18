package com.ramblingpenguin.icefloe.langchain.node;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmbeddingNodeTest {

    @Test
    void testFromString() {
        EmbeddingModel model = mock(EmbeddingModel.class);
        String text = "Hello";
        Embedding embedding = Embedding.from(new float[]{1.0f, 2.0f, 3.0f});
        Response<Embedding> response = Response.from(embedding);

        when(model.embed(anyString())).thenReturn(response);

        EmbeddingNode<String> node = EmbeddingNode.fromString(model);
        Response<Embedding> result = node.apply(text);

        assertEquals(embedding, result.content());
    }

    @Test
    void testFromTextSegment() {
        EmbeddingModel model = mock(EmbeddingModel.class);
        TextSegment segment = TextSegment.from("Hello");
        Embedding embedding = Embedding.from(new float[]{1.0f, 2.0f, 3.0f});
        Response<Embedding> response = Response.from(embedding);

        when(model.embed(any(TextSegment.class))).thenReturn(response);

        EmbeddingNode<TextSegment> node = EmbeddingNode.fromTextSegment(model);
        Response<Embedding> result = node.apply(segment);

        assertEquals(embedding, result.content());
    }
}

package com.ramblingpenguin.icefloe.aws.s3;

import com.ramblingpenguin.icefloe.core.Sequence;
import com.ramblingpenguin.rockhopper.s3.LocalStackS3Infrastructure;
import com.ramblingpenguin.rockhopper.s3.S3Bucket;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(LocalStackS3Infrastructure.class)
public class S3NodesTest {

    @S3Bucket(name = "test-bucket")
    Bucket bucket;

    @Test
    void testPutAndGet(S3Client s3Client) {
        S3PutNode putNode = new S3PutNode(s3Client);
        S3GetNode getNode = new S3GetNode(s3Client);

        String testContent = "Hello, S3!";
        String testKey = "test-object.txt";

        Sequence<S3Object, String> sequence = Sequence.Builder.of(S3Object.class)
                .then(putNode)
                .then(response -> new S3Object(bucket.name(), testKey))
                .then(getNode)
                .build();

        S3Object initialRequest = new S3Object(bucket.name(), testKey, RequestBody.fromString(testContent));
        String resultContent = sequence.apply(initialRequest);

        assertEquals(testContent, resultContent);
    }
}

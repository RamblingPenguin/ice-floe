package com.ramblingpenguin.icefloe.aws.s3;

import com.ramblingpenguin.icefloe.core.Node;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

/**
 * A node that retrieves an object from an Amazon S3 bucket.
 */
public class S3GetNode implements Node<S3Object, String> {

    private final S3Client s3Client;

    /**
     * Constructs a new S3GetNode.
     *
     * @param s3Client The S3Client to use for the operation.
     */
    public S3GetNode(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String apply(S3Object s3GetRequest) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3GetRequest.bucket())
                .key(s3GetRequest.key())
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
        try {
            return objectBytes.asUtf8String();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read S3 object content as UTF-8 string.", e);
        }
    }
}

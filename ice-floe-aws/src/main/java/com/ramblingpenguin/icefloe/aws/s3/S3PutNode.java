package com.ramblingpenguin.icefloe.aws.s3;

import com.ramblingpenguin.icefloe.core.Node;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

/**
 * A node that uploads an object to an Amazon S3 bucket.
 */
public class S3PutNode implements Node<S3Object, PutObjectResponse> {

    private final S3Client s3Client;

    /**
     * Constructs a new S3PutNode.
     *
     * @param s3Client The S3Client to use for the operation.
     */
    public S3PutNode(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public PutObjectResponse apply(S3Object s3PutRequest) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3PutRequest.bucket())
                .key(s3PutRequest.key())
                .build();

        return s3Client.putObject(putObjectRequest, s3PutRequest.requestBody());
    }
}

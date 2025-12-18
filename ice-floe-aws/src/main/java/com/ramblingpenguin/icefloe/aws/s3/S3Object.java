package com.ramblingpenguin.icefloe.aws.s3;

import software.amazon.awssdk.core.sync.RequestBody;

/**
 * A record representing the input for an S3 Get operation.
 *
 * @param bucket The name of the S3 bucket.
 * @param key    The key of the object to retrieve.
 */
public record S3Object(
        String bucket,
        String key,
        RequestBody requestBody
) {

    S3Object(String bucket, String key) {
        this(bucket, key, RequestBody.empty());
    }
}

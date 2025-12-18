# Ice Floe AWS

This module provides a collection of pre-built `Node`s for interacting with common AWS services, making it easy to integrate cloud operations into your Ice Floe pipelines.

## Design Principle

All nodes in this module are designed to be **stateless**. They accept an official AWS SDK v2 client (e.g., `S3Client`, `LambdaClient`) in their constructor. This allows you to manage the lifecycle of the AWS clients yourself and makes the nodes easy to test by injecting mock clients.

## Core Components

This module provides nodes for the following services:

*   **Amazon S3**: `S3PutNode`, `S3GetNode`
*   **AWS Lambda**: `LambdaInvokeNode`
*   **Amazon SQS**: `SqsSendNode`

## Usage Example

This example demonstrates a simple sequence that writes a piece of text to an S3 bucket and then reads it back.

```java
import com.ramblingpenguin.icefloe.aws.s3.S3GetNode;
import com.ramblingpenguin.icefloe.aws.s3.S3GetRequest;
import com.ramblingpenguin.icefloe.aws.s3.S3PutNode;
import com.ramblingpenguin.icefloe.aws.s3.S3PutRequest;
import com.ramblingpenguin.icefloe.core.Sequence;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

// Assume 's3Client' is an initialized S3Client instance

// 1. Create the node instances
S3PutNode putNode = new S3PutNode(s3Client);
S3GetNode getNode = new S3GetNode(s3Client);

String bucketName = "my-test-bucket";
String key = "my-object.txt";
String content = "Hello from Ice Floe!";

// 2. Build the sequence
Sequence<S3PutRequest, String> s3Pipeline = Sequence.Builder.of(S3PutRequest.class)
    // First, put the object
    .then(putNode)
    // The output of putNode is a PutObjectResponse. We ignore it and create a GetRequest.
    .then(putResponse -> new S3GetRequest(bucketName, key))
    // Finally, get the object content
    .then(getNode)
    .build();

// 3. Execute the sequence
S3PutRequest initialRequest = new S3PutRequest(bucketName, key, RequestBody.fromString(content));
String resultContent = s3Pipeline.apply(initialRequest);

// resultContent -> "Hello from Ice Floe!"
```

### Input and Output Types

Each node uses simple records for its input and returns either a standard AWS SDK response object or a primitive type.

*   **`S3PutNode`**:
    *   Input: `S3PutRequest(String bucket, String key, RequestBody requestBody)`
    *   Output: `PutObjectResponse`
*   **`S3GetNode`**:
    *   Input: `S3GetRequest(String bucket, String key)`
    *   Output: `String` (the object content as a UTF-8 string)
*   **`LambdaInvokeNode`**:
    *   Input: `LambdaInvokeRequest(String functionName, SdkBytes payload)`
    *   Output: `String` (the response payload as a UTF-8 string)
*   **`SqsSendNode`**:
    *   Input: `SqsSendRequest(String queueUrl, String messageBody)`
    *   Output: `SendMessageResponse`

package com.ramblingpenguin.icefloe.aws.sqs;

import com.ramblingpenguin.rockhopper.sqs.LocalStackSqsInfrastructure;
import com.ramblingpenguin.rockhopper.sqs.SqsQueue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(LocalStackSqsInfrastructure.class)
public class SqsSendNodeTest {

    @SqsQueue(queueName = "test-queue")
    String queueUrl;

    @Test
    void testSendMessage(SqsClient sqsClient) {
        SqsSendNode sendNode = new SqsSendNode(sqsClient);
        String messageBody = "Hello, SQS!";
        SqsSendRequest request = new SqsSendRequest(queueUrl, messageBody);

        sendNode.apply(request);

        String receivedMessage = sqsClient.receiveMessage(ReceiveMessageRequest.builder().queueUrl(queueUrl).build())
                .messages().get(0).body();

        assertEquals(messageBody, receivedMessage);
    }
}

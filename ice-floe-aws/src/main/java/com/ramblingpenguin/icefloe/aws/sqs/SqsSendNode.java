package com.ramblingpenguin.icefloe.aws.sqs;

import com.ramblingpenguin.icefloe.core.Node;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

/**
 * A node that sends a message to an Amazon SQS queue.
 */
public class SqsSendNode implements Node<SqsSendRequest, SendMessageResponse> {

    private final SqsClient sqsClient;

    /**
     * Constructs a new SqsSendNode.
     *
     * @param sqsClient The SqsClient to use for the operation.
     */
    public SqsSendNode(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    @Override
    public SendMessageResponse apply(SqsSendRequest sqsSendRequest) {
        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(sqsSendRequest.queueUrl())
                .messageBody(sqsSendRequest.messageBody())
                .build();

        return sqsClient.sendMessage(sendMessageRequest);
    }
}

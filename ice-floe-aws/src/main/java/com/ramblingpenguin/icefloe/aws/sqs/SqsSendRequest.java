package com.ramblingpenguin.icefloe.aws.sqs;

/**
 * A record representing the input for an SQS SendMessage operation.
 *
 * @param queueUrl    The URL of the SQS queue.
 * @param messageBody The body of the message to send.
 */
public record SqsSendRequest(
        String queueUrl,
        String messageBody
) {}

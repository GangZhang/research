package com.patsnap.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs;
import org.springframework.cloud.aws.messaging.config.annotation.NotificationMessage;
import org.springframework.cloud.aws.messaging.endpoint.annotation.NotificationMessageMapping;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * Author: Gang Zhang
 * Date: 2017/12/27
 */
@Component
public class NotificationSqsReceiveTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationSqsReceiveTest.class);

    @Value("${com.patsnap.sns.logout.subscriber.sqs}")
    private String subscriber;


    @SqsListener(value = "bo-test-sns-queue", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void sns2SqsListener(@Headers Map<String, String> headers, @NotificationMessage String rawJsonMessage)
            throws Exception {
        LOGGER.info("[SNS sqs-receiver] Subscribe logout msg in sqs: '{}'", rawJsonMessage);
    }


}

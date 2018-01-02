package com.patsnap.sns;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by IntelliJ IDEA.
 * Author: Gang Zhang
 * Date: 2017/12/27
 */
@Component
public class SnsPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(SnsPublisher.class);
    private AmazonSNS snsClient;

    @Autowired
    public SnsPublisher(AmazonSNS snsClient) {
        this.snsClient = snsClient;
    }

    public void publishMsg(String topicArn, String jsonMsg) {
        publishMsg(topicArn, jsonMsg, "");
    }

    public void publishMsg(String topicArn, String jsonMsg, String subject) {
        PublishRequest publishRequest = new PublishRequest(topicArn, jsonMsg, subject);
        PublishResult publishResult = snsClient.publish(publishRequest);
        //print MessageId of message published to SNS topic
        LOGGER.info("MessageId - " + publishResult.getMessageId());
    }
}

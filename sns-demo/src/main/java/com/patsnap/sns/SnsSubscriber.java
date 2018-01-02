package com.patsnap.sns;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.patsnap.endpoint.NotificationSubscribeTestController;
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
public class SnsSubscriber {
    private static final Logger LOGGER = LoggerFactory.getLogger(SnsSubscriber.class);
    private AmazonSNS snsClient;

    @Autowired
    public SnsSubscriber(AmazonSNS snsClient) {
        this.snsClient = snsClient;
    }

    public void subscribe(String topicArn, String protocol, String endpoint) {
        SubscribeRequest subscribeRequest = new SubscribeRequest(topicArn, protocol, endpoint);
        snsClient.subscribe(subscribeRequest);
        //get request id for SubscribeRequest from SNS metadata
        LOGGER.info("SubscribeRequest - " + snsClient.getCachedResponseMetadata(subscribeRequest));
    }

}

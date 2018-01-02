package com.patsnap.endpoint;

import com.amazonaws.util.StringUtils;
import com.patsnap.pojo.SubscriberInfo;
import com.patsnap.sns.SnsSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.config.annotation.NotificationMessage;
import org.springframework.cloud.aws.messaging.config.annotation.NotificationSubject;
import org.springframework.cloud.aws.messaging.endpoint.NotificationStatus;
import org.springframework.cloud.aws.messaging.endpoint.annotation.NotificationMessageMapping;
import org.springframework.cloud.aws.messaging.endpoint.annotation.NotificationSubscriptionMapping;
import org.springframework.cloud.aws.messaging.endpoint.annotation.NotificationUnsubscribeConfirmationMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * Author: Gang Zhang
 * Date: 2017/12/27
 */
@Controller
@RequestMapping("/subscribe/logout")
public class NotificationSubscribeTestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationSubscribeTestController.class);
    @Value("${com.patsnap.sns.logout.topic.name}")
    private String topicName;

    @Value("${com.patsnap.sns.logout.subscriber.endpoint}")
    private String endpoint;

    @Value("${com.patsnap.sns.logout.subscriber.protocal}")
    private String protocal;

    @Autowired
    private SnsSubscriber snsSubscriber;

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType
            .APPLICATION_JSON_VALUE)
    public ResponseEntity subscribe(HttpServletRequest request, HttpServletResponse response, @RequestBody SubscriberInfo
            subscriberInfo) {
        String subscriber = endpoint;
        if (subscriberInfo != null && !StringUtils.isNullOrEmpty(subscriberInfo.getEndpoint())) {
            subscriber = subscriberInfo.getEndpoint();
        }
        snsSubscriber.subscribe(topicName, protocal, subscriber);
        LOGGER.info("[SNS subscribe] Subscribe topic '{}' with endpoint '{}'.", topicName, subscriber);
        return new ResponseEntity("Subscribe success.", HttpStatus.OK);
    }

}

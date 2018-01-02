package com.patsnap.endpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.patsnap.pojo.LogoutInfo;
import com.patsnap.sns.SnsPublisher;
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
@RequestMapping("/publish/logout")
public class NotificationPublishTestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationPublishTestController.class);
    @Value("${com.patsnap.sns.logout.topic.name}")
    private String topicName;

    @Autowired
    private SnsPublisher snsPublisher;

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity publish(HttpServletRequest request, HttpServletResponse response, @RequestBody LogoutInfo
            logoutInfo) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String message = objectMapper.writeValueAsString(logoutInfo);
        snsPublisher.publishMsg(topicName, message);
        LOGGER.info("[SNS publish] Publish msg '{}' to topic '{}'.", message, topicName);
        return new ResponseEntity("Publish success.", HttpStatus.OK);
    }
}

package com.patsnap.sns;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;

/**
 * Hello world!
 */
public class SnsDemo {
    private static final String TOPIC_ARN = "arn:aws:sns:us-east-1:123456:bo-sns-test-logoutEvent";
    private static final String PROTOCAL = "http";
    private static final String ENDPOINT = "http://b1461b00.ngrok.io/demo/receive/logout";

    public static void main(String[] args) {
        //create a new SNS client and set endpoint
        AmazonSNS snsClient = AmazonSNSClientBuilder.standard().withCredentials(new
                ClasspathPropertiesFileCredentialsProvider()).withRegion(Regions.US_EAST_1).build();
//        AmazonSNSClient snsClient = new AmazonSNSClient(new ClasspathPropertiesFileCredentialsProvider());
//        snsClient.setRegion(Region.getRegion(Regions.US_EAST_2));

//        create a new SNS topic
//        CreateTopicRequest createTopicRequest = new CreateTopicRequest("LogoutEvent");
//        CreateTopicResult createTopicResult = snsClient.createTopic(createTopicRequest);
//        //print TopicArn
//        System.out.println(createTopicResult);
//        //get request id for CreateTopicRequest from SNS metadata
//        System.out.println("CreateTopicRequest - " + snsClient.getCachedResponseMetadata(createTopicRequest));

        SnsSubscriber snsSubscriber = new SnsSubscriber(snsClient);
        snsSubscriber.subscribe(TOPIC_ARN, PROTOCAL, ENDPOINT);

        //publish event
        SnsPublisher snsPublisher = new SnsPublisher(snsClient);
        String jsonMsg = "{\n" +
                "  \"username\": \"zhanggang@test.com\",\n" +
                "  \"from\": \"aws sns\"\n" +
                "}";
        snsPublisher.publishMsg(TOPIC_ARN, jsonMsg);

    }
}

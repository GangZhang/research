package com.patsnap.config;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.springframework.cloud.aws.messaging.config.QueueMessageHandlerFactory;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs;
import org.springframework.cloud.aws.messaging.endpoint.config.NotificationHandlerMethodArgumentResolverConfigurationUtils;
import org.springframework.cloud.aws.messaging.listener.QueueMessageHandler;
import org.springframework.cloud.aws.messaging.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.PayloadArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;

import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * Author: Gang Zhang
 * Date: 2017/12/28
 */
@Configuration
@EnableSqs
public class AwsAutoConfig {
    @Bean
    public AmazonSNS amazonSNSClient() {
//        AmazonSNS snsClient = AmazonSNSClientBuilder.standard().withCredentials(new
//                ClasspathPropertiesFileCredentialsProvider()).withRegion(Regions.CN_NORTH_1).build();
        AmazonSNS snsClient = AmazonSNSClientBuilder.standard().withEndpointConfiguration(new AwsClientBuilder
                .EndpointConfiguration("http://localhost:9911", Regions.US_EAST_1.getName())).build();
        return snsClient;
    }

    @Lazy
    @Bean(name = "amazonSQS", destroyMethod = "shutdown")
    public AmazonSQSAsync amazonSQSClient() {
//        AmazonSQSAsync  awsSQSAsyncClient = AmazonSQSAsyncClientBuilder.standard().withCredentials(new
//                ClasspathPropertiesFileCredentialsProvider()).withRegion(Regions.CN_NORTH_1).build();
        AmazonSQSAsync awsSQSAsyncClient = AmazonSQSAsyncClientBuilder.standard().withEndpointConfiguration(new
                AwsClientBuilder.EndpointConfiguration("http://localhost:4568", Regions.US_EAST_1.getName())).build();
        return awsSQSAsyncClient;
    }

    @Bean
    public HandlerMethodArgumentResolverComposite snsMethodArgumentResolver() throws Exception {
        return (HandlerMethodArgumentResolverComposite) NotificationHandlerMethodArgumentResolverConfigurationUtils
                .getNotificationHandlerMethodArgumentResolver(amazonSNSClient());
    }

    @Bean
    public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory(AmazonSQSAsync amazonSqs) {
        SimpleMessageListenerContainerFactory factory = new SimpleMessageListenerContainerFactory();
        factory.setAmazonSqs(amazonSqs);
        factory.setMaxNumberOfMessages(5);
        return factory;
    }

    @Bean
    public QueueMessageHandlerFactory queueMessageHandlerFactory() {
        QueueMessageHandlerFactory factory = new QueueMessageHandlerFactory();
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();

        //set strict content type match to false
        messageConverter.setStrictContentTypeMatch(false);
        factory.setArgumentResolvers(Collections.<HandlerMethodArgumentResolver>singletonList(new PayloadArgumentResolver(messageConverter)));
        return factory;
    }

    @Bean
    public QueueMessageHandler queueMessageHandler(AmazonSQSAsync amazonSQSAsync, QueueMessageHandlerFactory factory) {
//        QueueMessageHandlerFactory factory = new QueueMessageHandlerFactory();
        factory.setAmazonSqs(amazonSQSAsync);
        return factory.createQueueMessageHandler();
    }

    @Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer(SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory, QueueMessageHandler queueMessageHandler) {
        SimpleMessageListenerContainer container = simpleMessageListenerContainerFactory.createSimpleMessageListenerContainer();
        container.setMessageHandler(queueMessageHandler);
        container.setWaitTimeOut(20);
        return container;
    }
}

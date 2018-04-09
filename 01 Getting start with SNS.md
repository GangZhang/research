# Getting start with SNS
## Preface
> This topic describes how to create a SNS topic, subscribe an exist SNS topic, publish messages by the AWS SDK(also Spring Cloud Messaging easy to use).We make a simple demo to make publishing message to SQS/Http work.

## Maven Dependency
```
  <dependency>
      <groupId>com.amazonaws</groupId>
      <artifactId>aws-java-sdk-sns</artifactId>
      <version>1.11.254</version>
    </dependency>
        
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-aws-messaging</artifactId>
      <version>1.2.2.RELEASE</version>
    </dependency>
```

## What is SNS?
Amazon SNS is a publish-subscribe messaging system that allows clients to publish notification to a particular topic. Other interested clients may subscribe using different protocols like HTTP/HTTPS, e-mail or an Amazon SQS queue to receive the messages.

The next graphic shows a typical example of an Amazon SNS architecture.

![](http://cloud.spring.io/spring-cloud-aws/images/sns-overview.png)
Spring Cloud AWS supports Amazon SNS by providing support to send notifications with a `NotificationMessagingTemplate` and to receive notifications with the HTTP/HTTPS endpoint using the Spring Web MVC `@Controller` based programming model. Amazon SQS based subscriptions can be used with the annotation-driven message support that is provided by the Spring Cloud AWS messaging module.

## Getting start
### Get A SNS Client
An AmazonSNSClient constructor can use a credentials file called AwsCredentials.properties, which is found on the Java classpath.

Example: AwsCredentials.properties File Format
>accessKey=lDrDjH0D45hQivu6FNlwQ
secretKey=bHp5DOjg0HHJrGK7h3ejEqRDnVmWZK03T4lstel6

Creating an SNS Connection using AwsCredentials.properties in the Java Classpath:

```java
AmazonSNS snsClient = AmazonSNSClientBuilder.standard().withCredentials(new
                ClasspathPropertiesFileCredentialsProvider()).withRegion(Regions.US_EAST_1).build();
```

### Subscribe A Topic With HTTP Endpoint
SNS supports multiple endpoint types (SQS, Email, HTTP, HTTPS), Spring Cloud AWS provides support for HTTP(S) endpoints. 
To enable an Amazon SNS topic to send messages to an HTTP or HTTPS endpoint, [follow these steps](http://docs.amazonaws.cn/en_us/sns/latest/dg/SendMessageToHttp.html):

Step 1: Make sure your endpoint is ready to process Amazon SNS messages

Step 2: Subscribe the HTTP/HTTPS endpoint to the Amazon SNS topic

Step 3: Confirm the subscription

Step 4: Set the delivery retry policy for the subscription (optional)

Step 5: Give users permissions to publish to the topic (optional)

Step 6: Send messages to the HTTP/HTTPS endpoint

SNS sends three type of requests to an HTTP topic listener endpoint, for each of them annotations are provided:

* Subscription request → `@NotificationSubscriptionMapping`

* Notification request → `@NotificationMessageMapping`

* Unsubscription request → `@NotificationUnsubscribeMapping`

HTTP endpoints are based on Spring MVC controllers. Spring Cloud AWS added some custom argument resolvers to extract the message and subject out of the notification requests.
Controller as follow:

```java
/**
 * SNS receiver.
 * Author: Gang Zhang
 * Date: 2017/12/27
 */
@Controller
@RequestMapping("/receive/logout")
public class NotificationReceiveTestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationReceiveTestController.class);
    @NotificationSubscriptionMapping
    public void handleSubscriptionMessage(NotificationStatus status) throws IOException {
        //We subscribe to start receive the message
        status.confirmSubscription();
    }

    @NotificationMessageMapping
    public void handleNotificationMessage(@NotificationSubject String subject, @NotificationMessage String message) {
        LOGGER.info("[SNS http-receiver] Subscribe logout msg: {}", message);
    }

    @NotificationUnsubscribeConfirmationMapping
    public void handleUnsubscribeMessage(NotificationStatus status) {
        //e.g. the client has been unsubscribed and we want to "re-subscribe"
        status.confirmSubscription();
    }

}
```

You should config argument resolvers as follow:

```java
@Configuration
@EnableSqs
public class awsAutoConfig {
    @Bean
    public AmazonSNS amazonSNSClient() {
        AmazonSNS snsClient = AmazonSNSClientBuilder.standard().withCredentials(new
                ClasspathPropertiesFileCredentialsProvider()).withRegion(Regions.US_EAST_1).build();
        return snsClient;
    }

    @Bean
    public HandlerMethodArgumentResolverComposite snsMethodArgumentResolver() throws Exception {
        return  (HandlerMethodArgumentResolverComposite) NotificationHandlerMethodArgumentResolverConfigurationUtils
                .getNotificationHandlerMethodArgumentResolver(amazonSNSClient());
    }
    ...
}
```

Subscribe to the HTTP endpoint:

```java
private static final String TOPIC_ARN = "arn:aws:sns:us-east-1:1234576:bo-sns-test-logoutEvent";
private static final String PROTOCAL = "http";
private static final String ENDPOINT = "http://b1461b00.ngrok.io/demo/receive/logout";
...
SubscribeRequest subscribeRequest = new SubscribeRequest(topicArn, protocol, endpoint);
snsClient.subscribe(subscribeRequest);
//get request id for SubscribeRequest from SNS metadata
LOGGER.info("SubscribeRequest - " + snsClient.getCachedResponseMetadata(subscribeRequest));
```

### Publish Message
Publish message using AWS SDK, should use Spring Cloud messaging `NotificationMessagingTemplate` instead.

```java
 String jsonMsg = "{\n" +
                "  \"username\": \"zhanggang@test.com\",\n" +
                "  \"from\": \"aws sns\"\n" +
                "}";
PublishRequest publishRequest = new PublishRequest(topicArn, jsonMsg, subject);
PublishResult publishResult = snsClient.publish(publishRequest);
//print MessageId of message published to SNS topic
LOGGER.info("MessageId - " + publishResult.getMessageId());
```

### Additions
There are two ways for receiving SQS messages, either use the receive methods of the QueueMessagingTemplate or with annotation-driven listener endpoints. The latter is by far the more convenient way to receive messages.
Simply annotate methods with MessageMapping and the QueueMessageHandler will route the messages to the annotated methods.
Config MessageHandler bean as follow:

```java
    @Lazy
    @Bean(name = "amazonSQS", destroyMethod = "shutdown")
    public AmazonSQSAsync amazonSQSClient() {
        AmazonSQSAsync  awsSQSAsyncClient = new AmazonSQSAsyncClient(new DefaultAWSCredentialsProviderChain());

        awsSQSAsyncClient.setRegion(Region.getRegion(Regions.fromName("us-east-1")));
        return awsSQSAsyncClient;
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
    public QueueMessageHandler queueMessageHandler(AmazonSQSAsync amazonSQSAsync,QueueMessageHandlerFactory factory) {
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
    ...
```

In this example a queue listener container is started that polls the SQS queueName(`bo-test-sns-queue`) passed to the MessageMapping annotation. The incoming messages are converted to the target type and then the annotated method queueListener is invoked.

```java
@Component
public class NotificationSqsReceiveTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationSqsReceiveTest.class);

    @SqsListener(value = "bo-test-sns-queue", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void sns2SqsListener(@Headers Map<String, String> headers, @NotificationMessage String rawJsonMessage)
            throws Exception {
        LOGGER.info("[SNS sqs-receiver] Subscribe logout msg in sqs: '{}'", rawJsonMessage);
    }


}
```

## Support With Fake-sqs And Fake-sns
### Deploy Fake Services
We use [s12v/sns](https://github.com/s12v/sns) to deploy local fake sns service , and use [iain/fake_sqs](https://github.com/iain/fake_sqs) to deploy local fake sqs service, and test simple function like: `create-topic`,`create-queue`,`subscribe`, then intergrate with Java sdk to `publish` event and `receive` event.

* Fake SQS

	**Installation**
	>gem install fake_sqs
	
	**Running**
	> fake_sqs --database /path/to/database.yml
	

* Fake SNS
	
	**[Docker](https://github.com/s12v/sns#docker)**
	
	**Jar**
	
	Download the latest release from https://github.com/s12v/sns/releases and run:
	>DB_PATH=/tmp/db.json java -jar sns-0.3.7.jar
	
	Requires Java8.



### Create Topic
Create topic:
>aws sns --endpoint-url http://localhost:9911  create-topic --name bo-sso-logout-local>>"TopicArn": "arn:aws:sns:us-east-1:123456789012:bo-sso-logout-local"


### Create Queue
Create queue:
>aws sqs --endpoint-url http://localhost:4568 create-queue --queue-name course-sso-logout-queue-local```json"QueueUrls": [        "http://localhost:4568/course-sso-logout-queue-local"    ]
```

Get queue arn:
>aws sqs --endpoint-url http://localhost:4568 get-queue-attributes --queue-url http://localhost:4568/course-sso-logout-queue-local --attribute-names All```json	
	{    "Attributes": {        "ApproximateNumberOfMessagesNotVisible": "0",        "ApproximateNumberOfMessages": "0",        "QueueArn": "arn:aws:sqs:us-east-1:06e16ad806a494e8a5841c522a804365:course-sso-logout-queue-local"    	}	}
```


### Subscribe Sqs
Subscribe sns with sqs
>aws sns --endpoint-url http://localhost:9911 subscribe --topic-arn arn:aws:sns:us-east-1:123456789012:bo-sso-logout-local --protocol sqs --notification-endpoint *"aws-sqs://course-sso-logout-queue-local?amazonSQSEndpoint=http://localhost:4568&accessKey=&secretKey="*```json{    "SubscriptionArn": "arn:aws:sns:us-east-1:123456789012:bo-sso-logout-local:83bf13d8-e7c5-4435-9730-b90e4791b4e0"}
```
#### Attention
* Aws cli subscribe `--notification-endpoint`  should use [Camel Uri](https://github.com/s12v/sns), refer to [Supported integrations](https://github.com/s12v/sns#supported-integrations).

### Local Service Config
#### AwsAutoConfig 

Use EndpointConfiguration to config aws client:

Local fake sns endpoint :`http://localhost:9911`

Local fake sqs endpoint :`http://localhost:4568`

```java
@Configuration
@EnableSqs
public class AwsAutoConfig {
    @Bean
    public AmazonSNS amazonSNSClient() {
        AmazonSNS snsClient = AmazonSNSClientBuilder.standard().withEndpointConfiguration(new AwsClientBuilder
                .EndpointConfiguration("http://localhost:9911", Regions.US_EAST_1.getName())).build();
        return snsClient;
    }

    @Lazy
    @Bean(name = "amazonSQS", destroyMethod = "shutdown")
    public AmazonSQSAsync amazonSQSClient() {
        AmazonSQSAsync awsSQSAsyncClient = AmazonSQSAsyncClientBuilder.standard().withEndpointConfiguration(new
                AwsClientBuilder.EndpointConfiguration("http://localhost:4568", Regions.US_EAST_1.getName())).build();
        return awsSQSAsyncClient;
    }
    ...
 }
```

#### Sqs listener

The SQS queueName(`bo-test-sns-queue`) should change to local queueUrl(`http://localhost:4568/course-sso-logout-queue-local`). 

```java
    @SqsListener(value = "http://localhost:4568/course-sso-logout-queue-local", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    public void sns2SqsListener(@Headers Map<String, String> headers, @NotificationMessage String rawJsonMessage)
            throws Exception {
        LOGGER.info("[SNS sqs-receiver] Subscribe logout msg in sqs: '{}'", rawJsonMessage);
    }
```

#### Attention
* Get Error as follow , but do not affect the subscribe/publish :
![](https://github.com/GangZhang/research/blob/Develop-fake-sns-sqs/01%20SNS/images/Sqs%20error.png?raw=true)This is an known issue about fake sqs , can be saw [here](https://github.com/iain/fake_sqs/issues/40).


## GitHub

Get SNS DEMO on GitHub [Here&hearts;](https://github.com/GangZhang/research/tree/master/01%20SNS)

Support with fake-sns & fake-sqs DEMO [Here&spades;](https://github.com/GangZhang/research/tree/Develop-fake-sns-sqs/01%20SNS)

## References
[1] [http://cloud.spring.io/spring-cloud-aws/spring-cloud-aws.html](http://cloud.spring.io/spring-cloud-aws/spring-cloud-aws.html#_receiving_a_message)

[2] [Credentials set in AwsCredentials.properties file](https://www.ibm.com/support/knowledgecenter/STXNRM_3.11.1/coss.doc/aws_java_c_cred_file.html)

## Spring Integration

Spring Framework on Google Cloud provides Spring Integration adapters that allow your
applications to use Enterprise Integration Patterns backed up by Google
Cloud services.

### Channel Adapters for Cloud Pub/Sub

The channel adapters for Google Cloud Pub/Sub connect your Spring
[`MessageChannels`](https://docs.spring.io/spring-integration/reference/html/channel.html)
to Google Cloud Pub/Sub topics and subscriptions. This enables messaging
between different processes, applications or microservices backed up by
Google Cloud Pub/Sub.

The Spring Integration Channel Adapters for Google Cloud Pub/Sub are
included in the `spring-cloud-gcp-pubsub` module and can be
autoconfigured by using the `spring-cloud-gcp-starter-pubsub` module in
combination with a Spring Integration dependency.

Maven coordinates,
using [Spring Framework on Google Cloud BOM](getting-started.xml#bill-of-materials):

``` xml
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>spring-cloud-gcp-starter-pubsub</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.integration</groupId>
    <artifactId>spring-integration-core</artifactId>
</dependency>
```

Gradle coordinates:

    dependencies {
        implementation("com.google.cloud:spring-cloud-gcp-starter-pubsub")
        implementation("org.springframework.integration:spring-integration-core")
    }

#### Inbound channel adapter (using Pub/Sub Streaming Pull)

`PubSubInboundChannelAdapter` is the inbound channel adapter for GCP
Pub/Sub that listens to a Spring Framework on Google Cloud Pub/Sub subscription for new messages. It
converts new messages to an internal Spring
[`Message`](https://docs.spring.io/spring-integration/reference/html/messaging-construction-chapter.html#message)
and then sends it to the bound output channel.

Google Pub/Sub treats message payloads as byte arrays. So, by default,
the inbound channel adapter will construct the Spring `Message` with
`byte[]` as the payload. However, you can change the desired payload
type by setting the `payloadType` property of the
`PubSubInboundChannelAdapter`. The `PubSubInboundChannelAdapter`
delegates the conversion to the desired payload type to the
`PubSubMessageConverter` configured in the `PubSubTemplate`.

To use the inbound channel adapter, a `PubSubInboundChannelAdapter` must
be provided and configured on the user application side.
> **NOTE:** The subscription name could either be a short subscription name within the current project, or the fully-qualified name referring to a subscription in a different project using the `projects/[project_name]/subscriptions/[subscription_name]` format.


``` java
@Bean
public MessageChannel pubsubInputChannel() {
    return new PublishSubscribeChannel();
}

@Bean
public PubSubInboundChannelAdapter messageChannelAdapter(
    @Qualifier("pubsubInputChannel") MessageChannel inputChannel,
    PubSubTemplate pubsubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubsubTemplate, "subscriptionName");
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.MANUAL);

    return adapter;
}
```

In the example, we first specify the `MessageChannel` where the adapter
is going to write incoming messages to. The `MessageChannel`
implementation isn’t important here. Depending on your use case, you
might want to use a `MessageChannel` other than
`PublishSubscribeChannel`.

Then, we declare a `PubSubInboundChannelAdapter` bean. It requires the
channel we just created and a `SubscriberFactory`, which creates
`Subscriber` objects from the Google Cloud Java Client for Pub/Sub. The
Spring Boot starter for Spring Framework on Google Cloud Pub/Sub provides a configured
`PubSubSubscriberOperations` object.

##### Acknowledging messages and handling failures

When working with Cloud Pub/Sub, it is important to understand the
concept of `ackDeadline` — the amount of time Cloud Pub/Sub will wait
until attempting redelivery of an outstanding message. Each subscription
has a default `ackDeadline` applied to all messages sent to it.
Additionally, the Cloud Pub/Sub client library can extend each streamed
message’s `ackDeadline` until the message processing completes, fails or
until the maximum extension period elapses.

<div class="note">

In the Pub/Sub client library, default maximum extension period is an
hour. However, Spring Framework on Google Cloud disables this auto-extension behavior.
Use the `spring.cloud.gcp.pubsub.subscriber.max-ack-extension-period`
property to re-enable it.

</div>

Acknowledging (acking) a message removes it from Pub/Sub’s known
outstanding messages. Nacking a message resets its acknowledgement
deadline to 0, forcing immediate redelivery. This could be useful in a
load balanced architecture, where one of the subscribers is having
issues but others are available to process messages.

The `PubSubInboundChannelAdapter` supports three acknowledgement modes:
the default `AckMode.AUTO` (automatic acking on processing success and
nacking on exception), as well as two modes for additional manual
control: AckMode.AUTO\_ACK (automatic acking on success but no action on
exception) and AckMode.MANUAL (no automatic actions at all; both acking
and nacking have to be done manually).

|                                                                        | AUTO                       | AUTO\_ACK          | MANUAL          |
| ---------------------------------------------------------------------- | -------------------------- | ------------------ | --------------- |
| Message processing completes successfully                              | ack, no redelivery         | ack, no redelivery | \<no action\>\* |
| Message processing fails, but error handler completes successfully\*\* | ack, no redelivery         | ack, no redelivery | \<no action\>\* |
| Message processing fails; no error handler present                     | nack, immediate redelivery | \<no action\>\*    | \<no action\>\* |
| Message processing fails, and error handler throws an exception        | nack, immediate redelivery | \<no action\>\*    | \<no action\>\* |

Acknowledgement mode behavior

\* \<no action\> means that the message will be neither acked nor
nacked. Cloud Pub/Sub will attempt redelivery according to subscription
`ackDeadline` setting and the `max-ack-extension-period` client library
setting.

\*\* For the adapter, "success" means the Spring Integration flow
processed without raising an exception, so successful message processing
and the successful completion of an error handler both result in the
same behavior (message will be acknowledged). To trigger default error
behavior (nacking in `AUTO` mode; neither acking nor nacking in
`AUTO_ACK` mode), propagate the error back to the adapter by throwing an
exception from the [Error Handling flow](#_error_handling).

###### Manual acking/nacking

The adapter attaches a `BasicAcknowledgeablePubsubMessage` object to the
`Message` headers. Users can extract the
`BasicAcknowledgeablePubsubMessage` using the
`GcpPubSubHeaders.ORIGINAL_MESSAGE` key and use it to ack (or nack) a
message.

``` java
@Bean
@ServiceActivator(inputChannel = "pubsubInputChannel")
public MessageHandler messageReceiver() {
    return message -> {
        LOGGER.info("Message arrived! Payload: " + new String((byte[]) message.getPayload()));
        BasicAcknowledgeablePubsubMessage originalMessage =
              message.getHeaders().get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
        originalMessage.ack();
    };
}
```

###### Error Handling

If you want to have more control over message processing in case of an
error, you need to associate the `PubSubInboundChannelAdapter` with a
Spring Integration error channel and specify the behavior to be invoked
with `@ServiceActivator`.

<div class="note">

In order to activate the default behavior (nacking in `AUTO` mode;
neither acking nor nacking in `AUTO_ACK` mode), your error handler has
to throw an exception. Otherwise, the adapter will assume that
processing completed successfully and will ack the message.

</div>

``` java
@Bean
public MessageChannel pubsubInputChannel() {
    return new PublishSubscribeChannel();
}

@Bean
public PubSubInboundChannelAdapter messageChannelAdapter(
    @Qualifier("pubsubInputChannel") MessageChannel inputChannel,
    PubSubTemplate pubsubTemplate) {
    PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubsubTemplate, "subscriptionName");
    adapter.setOutputChannel(inputChannel);
    adapter.setAckMode(AckMode.AUTO_ACK);
    adapter.setErrorChannelName("pubsubErrors");

    return adapter;
}

@ServiceActivator(inputChannel =  "pubsubErrors")
public void pubsubErrorHandler(Message<MessagingException> message) {
    LOGGER.warn("This message will be automatically acked because error handler completes successfully");
}
```

If you preferred to manually ack or nack the message, you can do it
by retrieving the header of the exception payload:

``` java
@ServiceActivator(inputChannel =  "pubsubErrors")
public void pubsubErrorHandler(Message<MessagingException> exceptionMessage) {

    BasicAcknowledgeablePubsubMessage originalMessage =
      (BasicAcknowledgeablePubsubMessage)exceptionMessage.getPayload().getFailedMessage()
        .getHeaders().get(GcpPubSubHeaders.ORIGINAL_MESSAGE);

    originalMessage.nack();
}
```

#### Pollable Message Source (using Pub/Sub Synchronous Pull)

While `PubSubInboundChannelAdapter`, through the underlying Asynchronous
Pull Pub/Sub mechanism, provides the best performance for high-volume
applications that receive a steady flow of messages, it can create load
balancing anomalies due to message caching. This behavior is most
obvious when publishing a large batch of small messages that take a long
time to process individually. It manifests as one subscriber taking up
most messages, even if multiple subscribers are available to take on the
work. For a more detailed explanation of this scenario, see [Spring Framework on Google Cloud Pub/Sub
documentation](https://cloud.google.com/pubsub/docs/pull#streamingpull_dealing_with_large_backlogs_of_small_messages).

In such a scenario, a `PubSubMessageSource` can help spread the load
between different subscribers more evenly.

As with the Inbound Channel Adapter, the message source has a
configurable acknowledgement mode, payload type, and header mapping.

The default behavior is to return from the synchronous pull operation
immediately if no messages are present. This can be overridden by using
`setBlockOnPull()` method to wait for at least one message to arrive.

By default, `PubSubMessageSource` pulls from the subscription one
message at a time. To pull a batch of messages on each request, use the
`setMaxFetchSize()` method to set the batch size.

> **NOTE:** The subscription name could either be a short subscription name within the current project, or the fully-qualified name referring to a subscription in a different project using the `projects/[project_name]/subscriptions/[subscription_name]` format.


``` java
@Bean
@InboundChannelAdapter(channel = "pubsubInputChannel", poller = @Poller(fixedDelay = "100"))
public MessageSource<Object> pubsubAdapter(PubSubTemplate pubSubTemplate) {
    PubSubMessageSource messageSource = new PubSubMessageSource(pubSubTemplate,  "exampleSubscription");
    messageSource.setAckMode(AckMode.MANUAL);
    messageSource.setPayloadType(String.class);
    messageSource.setBlockOnPull(true);
    messageSource.setMaxFetchSize(100);
    return messageSource;
}
```

The `@InboundChannelAdapter` annotation above ensures that the
configured `MessageSource` is polled for messages, which are then
available for manipulation with any Spring Integration mechanism on the
`pubsubInputChannel` message channel. For example, messages can be
retrieved in a method annotated with `@ServiceActivator`, as seen below.

For additional flexibility, `PubSubMessageSource` attaches an
`AcknowledgeablePubSubMessage` object to the
`GcpPubSubHeaders.ORIGINAL_MESSAGE` message header. The object can be
used for manually (n)acking the message.

``` java
@ServiceActivator(inputChannel = "pubsubInputChannel")
public void messageReceiver(String payload,
        @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) AcknowledgeablePubsubMessage message)
            throws InterruptedException {
    LOGGER.info("Message arrived by Synchronous Pull! Payload: " + payload);
    message.ack();
}
```

<div class="note">

`AcknowledgeablePubSubMessage` objects acquired by synchronous pull are
aware of their own acknowledgement IDs. Streaming pull does not expose
this information due to limitations of the underlying API, and returns
`BasicAcknowledgeablePubsubMessage` objects that allow acking/nacking
individual messages, but not extracting acknowledgement IDs for future
processing.

</div>

#### Outbound channel adapter

`PubSubMessageHandler` is the outbound channel adapter for Spring Framework on Google Cloud Pub/Sub
that listens for new messages on a Spring `MessageChannel`. It uses
`PubSubTemplate` to post them to a Spring Framework on Google Cloud Pub/Sub topic.

To construct a Pub/Sub representation of the message, the outbound
channel adapter needs to convert the Spring `Message` payload to a byte
array representation expected by Pub/Sub. It delegates this conversion
to the `PubSubTemplate`. To customize the conversion, you can specify a
`PubSubMessageConverter` in the `PubSubTemplate` that should convert the
`Object` payload and headers of the Spring `Message` to a
`PubsubMessage`.

To use the outbound channel adapter, a `PubSubMessageHandler` bean must
be provided and configured on the user application side.

> **NOTE:**  The topic name could either be a short topic name within the current project, or the fully-qualified name referring to a topic in a different project using the `projects/[project_name]/topics/[topic_name]` format.

``` java
@Bean
@ServiceActivator(inputChannel = "pubsubOutputChannel")
public MessageHandler messageSender(PubSubTemplate pubsubTemplate) {
    return new PubSubMessageHandler(pubsubTemplate, "topicName");
}
```

The provided `PubSubTemplate` contains all the necessary configuration
to publish messages to a Spring Framework on Google Cloud Pub/Sub topic.

`PubSubMessageHandler` publishes messages asynchronously by default. A
publish timeout can be configured for synchronous publishing. If none is
provided, the adapter waits indefinitely for a response.

It is possible to set user-defined callbacks for the `publish()` call in
`PubSubMessageHandler` through the `setSuccessCallback()` and
`setFailureCallback()` methods (either one or both may be set). These
give access to the Pub/Sub publish message ID in case of success, or the
root cause exception in case of error. Both callbacks include the
original message as the second argument.

``` java
adapter.setSuccessCallback((ackId, message) -> {});
adapter.setFailureCallback((cause, message) -> {});
```

To override the default topic you can use the `GcpPubSubHeaders.TOPIC`
header.

``` java
@Autowired
private MessageChannel pubsubOutputChannel;

public void handleMessage(Message<?> msg) throws MessagingException {
    final Message<?> message = MessageBuilder
        .withPayload(msg.getPayload())
        .setHeader(GcpPubSubHeaders.TOPIC, "customTopic").build();
    pubsubOutputChannel.send(message);
}
```

It is also possible to set an SpEL expression for the topic with the
`setTopicExpression()` or `setTopicExpressionString()` methods.

``` java
PubSubMessageHandler adapter = new PubSubMessageHandler(pubSubTemplate, "myDefaultTopic");
adapter.setTopicExpressionString("headers['sendToTopic']");
```

#### Header mapping

These channel adapters contain header mappers that allow you to map, or
filter out, headers from Spring to Google Cloud Pub/Sub messages, and
vice-versa. By default, the inbound channel adapter maps every header on
the Google Cloud Pub/Sub messages to the Spring messages produced by the
adapter. The outbound channel adapter maps every header from Spring
messages into Google Cloud Pub/Sub ones, except the ones added by Spring
and some special headers, like headers with key `"id"`, `"timestamp"`,
`"gcp_pubsub_acknowledgement"`, and `"gcp_pubsub_ordering_key"`. In the
process, the outbound mapper also converts the value of the headers into
string.

Note that you can provide the `GcpPubSubHeaders.ORDERING_KEY`
(`"gcp_pubsub_ordering_key"`) header, which will be automatically mapped
to `PubsubMessage.orderingKey` property, and excluded from the headers
in the published message. Remember to set
`spring.cloud.gcp.pubsub.publisher.enable-message-ordering` to `true`,
if you are publishing messages with this header.

Each adapter declares a `setHeaderMapper()` method to let you further
customize which headers you want to map from Spring to Google Cloud
Pub/Sub, and vice-versa.

For example, to filter out headers `"foo"`, `"bar"` and all headers
starting with the prefix "prefix\_", you can use `setHeaderMapper()`
along with the `PubSubHeaderMapper` implementation provided by this
module.

``` java
PubSubMessageHandler adapter = ...
...
PubSubHeaderMapper headerMapper = new PubSubHeaderMapper();
headerMapper.setOutboundHeaderPatterns("!foo", "!bar", "!prefix_*", "*");
adapter.setHeaderMapper(headerMapper);
```

<div class="note">

The order in which the patterns are declared in
`PubSubHeaderMapper.setOutboundHeaderPatterns()` and
`PubSubHeaderMapper.setInboundHeaderPatterns()` matters. The first
patterns have precedence over the following ones.

</div>

In the previous example, the `"*"` pattern means every header is mapped.
However, because it comes last in the list, [the previous patterns take
precedence](https://docs.spring.io/spring-integration/api/org/springframework/integration/support/utils/PatternMatchUtils.html).

#### Samples

Available examples:

  - [Sending/Receiving Messages with Channel
    Adapters](https://github.com/GoogleCloudPlatform/spring-cloud-gcp/tree/main/spring-cloud-gcp-samples/spring-cloud-gcp-integration-pubsub-sample)

  - [Pub/Sub Channel Adapters with JSON
    payloads](https://github.com/GoogleCloudPlatform/spring-cloud-gcp/tree/main/spring-cloud-gcp-samples/spring-cloud-gcp-integration-pubsub-json-sample)

  - [Spring Integration and Pub/Sub
    Codelab](https://codelabs.developers.google.com/codelabs/cloud-spring-cloud-gcp-pubsub-integration/index.html)

### Channel Adapters for Google Cloud Storage

The channel adapters for Google Cloud Storage allow you to read and
write files to Google Cloud Storage through `MessageChannels`.

Spring Framework on Google Cloud provides two inbound adapters,
`GcsInboundFileSynchronizingMessageSource` and
`GcsStreamingMessageSource`, and one outbound adapter,
`GcsMessageHandler`.

The Spring Integration Channel Adapters for Google Cloud Storage are
included in the `spring-cloud-gcp-storage` module.

To use the Storage portion of Spring Integration for Spring Framework on Google Cloud,
you must also provide the `spring-integration-file` dependency, since it
isn’t pulled transitively.

Maven coordinates,
using [Spring Framework on Google Cloud BOM](getting-started.xml#bill-of-materials):

``` xml
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>spring-cloud-gcp-storage</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.integration</groupId>
    <artifactId>spring-integration-file</artifactId>
</dependency>
```

Gradle coordinates:

    dependencies {
        implementation("com.google.cloud:spring-cloud-gcp-starter-storage")
        implementation("org.springframework.integration:spring-integration-file")
    }

#### Inbound channel adapter

The Google Cloud Storage inbound channel adapter polls a Google Cloud
Storage bucket for new files and sends each of them in a `Message`
payload to the `MessageChannel` specified in the
`@InboundChannelAdapter` annotation. The files are temporarily stored in
a folder in the local file system.

Here is an example of how to configure a Google Cloud Storage inbound
channel adapter.

``` java
@Bean
@InboundChannelAdapter(channel = "new-file-channel", poller = @Poller(fixedDelay = "5000"))
public MessageSource<File> synchronizerAdapter(Storage gcs) {
  GcsInboundFileSynchronizer synchronizer = new GcsInboundFileSynchronizer(gcs);
  synchronizer.setRemoteDirectory("your-gcs-bucket");

  GcsInboundFileSynchronizingMessageSource synchAdapter =
          new GcsInboundFileSynchronizingMessageSource(synchronizer);
  synchAdapter.setLocalDirectory(new File("local-directory"));

  return synchAdapter;
}
```

#### Inbound streaming channel adapter

The inbound streaming channel adapter is similar to the normal inbound
channel adapter, except it does not require files to be stored in the
file system.

Here is an example of how to configure a Google Cloud Storage inbound
streaming channel adapter.

``` java
@Bean
@InboundChannelAdapter(channel = "streaming-channel", poller = @Poller(fixedDelay = "5000"))
public MessageSource<InputStream> streamingAdapter(Storage gcs) {
  GcsStreamingMessageSource adapter =
          new GcsStreamingMessageSource(new GcsRemoteFileTemplate(new GcsSessionFactory(gcs)));
  adapter.setRemoteDirectory("your-gcs-bucket");
  return adapter;
}
```

If you would like to process the files in your bucket in a specific
order, you may pass in a `Comparator<BlobInfo>` to the constructor
`GcsStreamingMessageSource` to sort the files being processed.

#### Outbound channel adapter

The outbound channel adapter allows files to be written to Google Cloud
Storage. When it receives a `Message` containing a payload of type
`File`, it writes that file to the Google Cloud Storage bucket specified
in the adapter.

Here is an example of how to configure a Google Cloud Storage outbound
channel adapter.

``` java
@Bean
@ServiceActivator(inputChannel = "writeFiles")
public MessageHandler outboundChannelAdapter(Storage gcs) {
  GcsMessageHandler outboundChannelAdapter = new GcsMessageHandler(new GcsSessionFactory(gcs));
  outboundChannelAdapter.setRemoteDirectoryExpression(new ValueExpression<>("your-gcs-bucket"));

  return outboundChannelAdapter;
}
```

#### Sample

See the [Spring Integration with Google Cloud Storage Sample
Code](https://github.com/GoogleCloudPlatform/spring-cloud-gcp/tree/main/spring-cloud-gcp-samples/spring-cloud-gcp-integration-storage-sample).

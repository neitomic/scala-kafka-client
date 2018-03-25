# Scala Kafka Client
> A wrapped producer and consumer for Kafka

## Library
### Maven
```xml
<dependency>
    <groupId>com.github.thanhtien522</groupId>
    <artifactId>scala-kafka-client_2.11</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Sbt
```scala
libraryDependencies += "com.github.thanhtien522" %% "scala-kafka-client" % "0.1.0"
```

## How to use
### Producer

```scala
val producer = new ProducerBuilder[String, String]()
    .setBootstrapServers("<your bootstrap servers>")
    .setSerializer(new StringSerializer, new StringSerializer)
    .build()
  producer.send("topic", "Key", "Value")
```

Support config functions:
 - setBootstrapServers: Configure kafka bootstrap servers
 - setSerialize: Configure data serializer
 - setConfig(key, value): Configure with key-value pair
 - withConfig( typesafe config): Append configuration from TypeSafe Config object

### Consumer

```scala
val consumer = new ConsumerBuilder[String, String]()
    .setGroupId("group_id")
    .setClientId("client_id")
    .setBootstrapServers("servers")
    .setDeserializer(new StringDeserializer, new StringDeserializer)
    .setSubscribeTopics(Seq("topic"))
    .setPollInterval(100)
    .setConsumer(new KafkaRecordConsumer[String, String] {
      override def consume(record: ConsumerRecord[String, String]): Unit = {
        println(s"${record.topic()} - ${record.partition()} - ${record.offset()} - ${record.key()} - ${record.value()}")
      }
    })
    .build()

  consumer.start()
```

Support config functions:
 - setBootstrapServers: Configure kafka bootstrap servers
 - setDeserialize: Configure data deserializer
 - setPollInterval(itv: Int): Configure polling interval in milliseconds
 - setSubscribeTopics: Configure topic subscribe strategy
 - setGroupId: Configure consumer group id
 - setClientId: Configure consumer client id
 - setWarningCallback: Configure callback for ignored error
 - setFatalCallback: Configure callback for critical error
 - setConfig(key, value): Configure with key-value pair
 - withConfig( typesafe config): Append configuration from TypeSafe Config object

#### Topic subscribe strategy
 - Multiple topic subscribe: Subscribe multiple topic
 - Pattern topic subscribe: Subscribe topic name match pattern

#### Callback
All Exception that extend from KafkaException is considered as `Ignore error` and Warning Callback will be called
All others exception is considered as `critical error`, Fatal Callback will be called and consumer will be stop

## Monitoring
Future work
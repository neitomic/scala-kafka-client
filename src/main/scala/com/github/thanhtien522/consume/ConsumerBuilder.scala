package com.github.thanhtien522.consume

import java.util

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.clients.consumer.{ConsumerRebalanceListener, ConsumerRecord}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{Deserializer, StringDeserializer}

import scala.collection.mutable
import scala.util.matching.Regex
import scala.collection.JavaConversions._

class ConsumerBuilder[K, V]() {

  private var consumer: KafkaRecordConsumer[K, V] = _
  private val config = mutable.Map[String, Any](
    "enable.auto.commit" -> false
  )
  private var subscribeTopics: TopicSubscriber = _
  private var pollInterval: Option[Int] = None
  private var keyDeserializer: Deserializer[K] = _
  private var valueDeserializer: Deserializer[V] = _
  private var warningCallback: Throwable => Unit = _
  private var fatalCallback: Throwable => Unit = _
  private var name: String = _

  def setConsumer(consumer: KafkaRecordConsumer[K, V]): this.type = {
    this.consumer = consumer
    this
  }

  def withConfig(config: Config): this.type = {
    config.entrySet().foreach(e => this.config.put(e.getKey, e.getValue.unwrapped().toString))
    this
  }

  def setBootstrapServers(host: String): this.type = setConfig("bootstrap.servers", host)

  def setDeserializer(keyDeserializer: Deserializer[K], valueDeserializer: Deserializer[V]): this.type = {
    this.keyDeserializer = keyDeserializer
    this.valueDeserializer = valueDeserializer
    this
  }

  def setConfig(key: String, value: String): this.type = {
    this.config(key) = value
    this
  }

  def setPollInterval(itv: Int): this.type = {
    if (itv > 0)
      pollInterval = Some(itv)
    this
  }

  def setSubscribeTopics(topics: Seq[String]): this.type = {
    subscribeTopics = MultipleTopicSubscriber(topics)
    this
  }

  def setSubscribeTopics(pattern: Regex): this.type = {
    subscribeTopics = PatternTopicSubscriber(pattern)
    this
  }

  def setSubscribeTopics(pattern: Regex, onRevoked: Seq[TopicPartition] => Unit, onAssigned: Seq[TopicPartition] => Unit): this.type = {
    val listener = new ConsumerRebalanceListener {

      override def onPartitionsRevoked(collection: util.Collection[TopicPartition]): Unit = {
        onRevoked.apply(collection.toSeq)
      }

      override def onPartitionsAssigned(collection: util.Collection[TopicPartition]): Unit = {
        onAssigned.apply(collection.toSeq)
      }
    }
    subscribeTopics = PatternTopicSubscriber(pattern)
    this
  }

  def setGroupId(groupId: String): this.type = {
    if (groupId != null) {
      setConfig("group.id", groupId)
    }
    this
  }

  def setClientId(clientId: String): this.type = {
    if (clientId != null)
      setConfig("client.id", clientId)
    this
  }

  def setWarningCallback(fn: Throwable => Unit): this.type = {
    this.warningCallback = fn
    this
  }

  def setFatalCallback(fn: Throwable => Unit): this.type = {
    this.fatalCallback = fn
    this
  }

  def build(): KafkaConsumerWorker[K, V] = {
    new KafkaConsumerWorker(
      consumer, ConfigFactory.parseMap(this.config), subscribeTopics,
      keyDeserializer, valueDeserializer,
      pollInterval.getOrElse(100),
      Option(name),
      Option(warningCallback),
      Option(fatalCallback)
    )
  }
}

object ConsumerBuilder {
  def apply[K, V](): ConsumerBuilder[K, V] = new ConsumerBuilder[K, V]()
}


object Test extends App {
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
}
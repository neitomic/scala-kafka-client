package com.github.thanhtien522.produce

import cakesolutions.kafka.{KafkaProducer => CakeProducer}
import com.github.thanhtien522.TwitterConverters._
import com.twitter.util.Future
import com.typesafe.config.Config
import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.Serializer

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class KafkaProducer[K, V](config: Config,
                          keySerializer: Serializer[K],
                          valueSerializer: Serializer[V]) {

  val producer = CakeProducer(CakeProducer.Conf(config, keySerializer, valueSerializer))

  def send(topic: String, key: K, value: V, partition: java.lang.Integer = null, timestamp: java.lang.Long = null)
          (implicit ec: ExecutionContext = global): Future[RecordMetadata] = {
    producer.send(new ProducerRecord[K, V](topic, partition, timestamp, key, value))
  }

  def oneWaySend(topic: String, key: K, value: V, partition: java.lang.Integer = null, timestamp: java.lang.Long = null): Unit =
    producer.send(new ProducerRecord[K, V](topic, partition, timestamp, key, value))
}


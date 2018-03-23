package com.github.thanhtien522.produce

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.common.serialization.Serializer

import scala.collection.JavaConverters._
import scala.collection.mutable

class ProducerBuilder[K, V]() {

  var keySerializer: Serializer[K] = _
  var valueSerializer: Serializer[V] = _
  val config: mutable.Map[String, Any] = mutable.Map(
    "acks" -> "all",
    "retries" -> 0,
    "batch.size" -> 16384,
    "linger.ms" -> 1,
    "buffer.memory" -> 33554432
  )

  def withConfig(config: Config): this.type = {
    config.entrySet().asScala.foreach(entry =>
      this.config.put(entry.getKey, entry.getValue.unwrapped().toString))
    this
  }

  def setBootstrapServers(host: String): this.type = setConfig("bootstrap.servers", host)

  def setSerializer(keySerializer: Serializer[K], valueSerializer: Serializer[V]): this.type = {
    this.keySerializer = keySerializer
    this.valueSerializer = valueSerializer
    this
  }

  def setConfig(key: String, value: String): this.type = {
    this.config(key) = value
    this
  }

  def build(): KafkaProducer[K, V] = {
    new KafkaProducer[K, V](
      ConfigFactory.parseMap(this.config.asJava),
      keySerializer, valueSerializer)
  }
}
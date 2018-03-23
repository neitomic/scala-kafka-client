package com.github.thanhtien522.consume

import java.util.concurrent.atomic.AtomicBoolean

import cakesolutions.kafka.KafkaConsumer
import cakesolutions.kafka.KafkaConsumer.Conf
import com.typesafe.config.Config
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.{KafkaException, TopicPartition}
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._
import scala.collection.mutable

class KafkaConsumerWorker[K, V](
                                 consumer: KafkaRecordConsumer[K, V],
                                 config: Config,
                                 topics: TopicSubscriber,
                                 keyDeserializer: Deserializer[K],
                                 valueDeserializer: Deserializer[V],
                                 val polInterval: Int = 100,
                                 name: Option[String] = None,
                                 warningCallback: Option[Throwable => Unit] = None,
                                 fatalCallback: Option[Throwable => Unit] = None
                               ) {

  final val logger = LoggerFactory.getLogger(classOf[KafkaConsumerWorker[K, V]])

  private val kafkaConsumer = KafkaConsumer[K, V](Conf(config, keyDeserializer, valueDeserializer))

  topics match {
    case MultipleTopicSubscriber(topicNames) => kafkaConsumer.subscribe(topicNames)
    case PatternTopicSubscriber(p, listener) => kafkaConsumer.subscribe(p.pattern, listener)
  }

  private val running: AtomicBoolean = new AtomicBoolean(false)

  def start(): Thread = {
    running.set(true)
    val thread = new Thread(runnable)
    if (name.isDefined) thread.setName(name.get)
    thread.start()
    thread
  }

  private def handleCallback(throwable: Throwable, callback: Option[Throwable => Unit]): Unit = {
    try {
      if (callback.isDefined) callback.get.apply(throwable)
    }
    catch {
      case callbackError: Throwable => logger.error("Callback error", callbackError)
    }
  }

  def stop(): Unit = running.set(false)

  lazy val runnable: Runnable = new Runnable {
    override def run(): Unit = {

      val offsets = mutable.Map[TopicPartition, Long]()
      var hasNewRecord: Boolean = false
      while (running.get()) {
        try {
          try {
            val records = kafkaConsumer.poll(polInterval)
            for (record <- records) {
              hasNewRecord = true
              consumer.consume(record)
              offsets(new TopicPartition(record.topic(), record.partition())) = record.offset() + 1
            }
          } finally {
            if (hasNewRecord) {
              kafkaConsumer.commitSync(offsets.map(e => (e._1, new OffsetAndMetadata(e._2))))
              hasNewRecord = false
              logger.info(s"Committed offset $offsets")
            }
          }
        } catch {
          case kafkaException: KafkaException =>
            // Continue to process.
            // This solution might be cause duplicate record return with poll.
            // But, when error happen in commit offset to Kafka brokers,
            // the consumer will be rebalanced and the records also duplicate.
            // So, we keep this consumer rejoin to the group.
            logger.warn("Kafka exception. Fallback to polling records.", kafkaException)
            handleCallback(kafkaException, warningCallback)

          case e: Throwable =>
            logger.error("Kafka consumer failure. Consumer worker will be stopped", e)
            stop() //Stop the worker
            handleCallback(e, fatalCallback)
        }
      }
    }
  }
}
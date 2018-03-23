package com.github.thanhtien522.consume

import org.apache.kafka.clients.consumer.ConsumerRecord

trait KafkaRecordConsumer[K, V] {
  def consume(record: ConsumerRecord[K, V])
}
package com.github.thanhtien522.consume

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener
import org.apache.kafka.clients.consumer.internals.NoOpConsumerRebalanceListener

import scala.util.matching.Regex

trait TopicSubscriber

case class MultipleTopicSubscriber(topics: Seq[String]) extends TopicSubscriber

case class PatternTopicSubscriber(topicPattern: Regex,
                                  rebalanceListener: ConsumerRebalanceListener = new NoOpConsumerRebalanceListener) extends TopicSubscriber


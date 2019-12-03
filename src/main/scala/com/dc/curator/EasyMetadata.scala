package com.dc.curator

import java.util.Properties

import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.{ KafkaStreams, StreamsConfig }
import org.apache.kafka.clients.consumer.{ ConsumerConfig }
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.scala.ImplicitConversions._

import Serdes._
import scala.collection.mutable.ArrayBuffer
import org.apache.kafka.streams.state.QueryableStoreTypes

class EasyMetadata(message: String) {

  def sayHi(): String = {
    println("Log Hello " ++ message)
    // "HHHH"
    "Hello " + message
  }

  var builder: StreamsBuilder = new StreamsBuilder()
  var streams: KafkaStreams = null
  var stateTable: KTable[String, String] = null

  def startKafka() {

    val props: Properties = {
      val p = new Properties()
      p.put(StreamsConfig.APPLICATION_ID_CONFIG, "trade-book1")
      p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, sys.env("KAFKA_SERVICE_HOST") + ":9092")
      p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
      p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
      p.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, "1")
      p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      p
    }

    stateTable = builder.table[String, String]("CTVTOATABLE", Materialized.as[String, String, ByteArrayKeyValueStore]("FINAL"))
    streams = new KafkaStreams(builder.build(), props)

    streams.cleanUp()
    streams.start()
    // streams.allMetadata()
  }

  def getData() {
    val finalState = streams.store(
      "FINAL",
      QueryableStoreTypes.keyValueStore[String, String]())

    // realise state to an ArrayBuffer just to prove the concept
    val stateData = finalState.all
    var currentState = ArrayBuffer[String]()
    while (stateData.hasNext) {
      val row = stateData.next
      currentState += row.value
    }
    // close the store hook
    stateData.close()
  }

  def stopKafka() = {
    streams.close()

    "Application stoppped"
  }
}

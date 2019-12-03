package com.dc.curator

import java.util.Properties

import org.scalatra._

// JSON-related libraries
import org.json4s.{ DefaultFormats, Formats }

// JSON handling support from Scalatra
import org.scalatra.json._
import org.slf4j.{Logger, LoggerFactory}

import org.apache.kafka.streams.errors.{InvalidStateStoreException}
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.{ KafkaStreams, StreamsConfig }
import org.apache.kafka.clients.consumer.{ ConsumerConfig }
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.kstream.{GlobalKTable, Materialized}
import org.apache.kafka.streams.scala.ImplicitConversions._

import Serdes._
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer
import org.apache.kafka.streams.state.{QueryableStoreTypes, ReadOnlyKeyValueStore}
import org.apache.kafka.streams.state.StreamsMetadata

import scala.collection.JavaConverters._

import com.github.nscala_time.time.Imports.DateTime
import com.github.nscala_time.time.Imports.DateTimeZone
import org.joda.time.Days
import scala.math.abs
// import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.write
import scala.util.Try
// import scala.collection


case class allData(UUID: Option[String], CONTENT: Option[String], SENTIMENT: Option[String], PROFANITY: Option[String], PERSONAL: Option[String], PREFERENCE: Option[String])
case class trainingData(UUID: Option[String], CONTENT: Option[String], PREFERENCE: Option[String])

class MyScalatraServlet extends ScalatraServlet with JacksonJsonSupport {

  var builder: StreamsBuilder = new StreamsBuilder()

  val props: Properties = {
    val p = new Properties()
    p.put(StreamsConfig.APPLICATION_SERVER_CONFIG, "localhost:8080")
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, "data_curator_1")
    p.put(StreamsConfig.CLIENT_ID_CONFIG, "data_curator-scala-client_1")
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
    p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
    p.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, "1")
    p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    p
  }

  var feedState: GlobalKTable[String, String] = builder.globalTable[String, String]("PREVIEW4", Materialized.as[String, String, ByteArrayKeyValueStore]("FINALVIEW"))
  var trainingState: GlobalKTable[String, String] = builder.globalTable[String, String]("PREFERENCE", Materialized.as[String, String, ByteArrayKeyValueStore]("TRAININGVIEW"))

  var streams: KafkaStreams = new KafkaStreams(builder.build(), props)

  
  //var originalContent_bytes: KStream[Array[Byte], String] = builder.stream[Array[Byte], String]("content_curator_twitter")
  //    .peek((k,v) => println(v))

  streams.cleanUp()
  println("Starting streams")
  streams.start()

  // wrapper for state store materialisations
  def getStore[K, V](storeName: String): ReadOnlyKeyValueStore[String, String] = {
    streams.store(storeName, QueryableStoreTypes.keyValueStore[String, String]())
  }


  // Before every action runs, set the content type to be in JSON format.
  protected implicit lazy val jsonFormats: Formats = DefaultFormats.withBigDecimal

  before() {
    contentType = formats("json")
  }

  get("/test") {
    "BANG"
  }

  get("/status") {
    "STATUS_RESPONSE"
  }

  get("/all") {

      val stateData = getStore("FINALVIEW")
      val stateRecords = stateData.all
      var records = ListBuffer[allData]()
      // collect records into an array buffer for filter operations
      while (stateRecords.hasNext) {
        val row = stateRecords.next
        println(row)
        val jsonData = parse(row.value)
        val parsed = Try(jsonData.extract[allData])
        val r = parsed.get
        records += r
      }
      stateRecords.close()
      records
  }

  get("/training") {

      val stateData = getStore("TRAININGVIEW")
      val stateRecords = stateData.all
      var records = ListBuffer[trainingData]()
      // collect records into an array buffer for filter operations
      while (stateRecords.hasNext) {
        val row = stateRecords.next
        println(row)
        val jsonData = parse(row.value)
        val parsed = Try(jsonData.extract[trainingData])
        val r = parsed.get
        records += r
      }
      stateRecords.close()
      records
  }
}

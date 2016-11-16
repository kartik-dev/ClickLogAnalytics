package com.demo.spark

import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions.desc
import org.apache.spark.streaming.Minutes
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.kafka.KafkaUtils

import io.confluent.kafka.serializers.KafkaAvroDecoder

case class ClickLog(user_id: Integer, time: String, action: String, destination: String, hotel: String)

object ClickLogStreamingJob {

  def main(args: Array[String]) {

    val sparkConf = new SparkConf().setAppName("ClickLogStreamingJob")
    sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    sparkConf.registerKryoClasses(Array(classOf[org.apache.avro.generic.GenericData]))
    sparkConf.set("spark.sql.tungsten.enabled", "true")
    sparkConf.set("spark.eventLog.enabled", "true")
    sparkConf.set("spark.io.compression.codec", "snappy")
    sparkConf.set("spark.rdd.compress", "true")
    sparkConf.set("spark.streaming.backpressure.enabled", "true")

    val sc = new SparkContext(sparkConf)
    sc.setLogLevel("ERROR")

    val ssc = new StreamingContext(sc, Seconds(10))

    // TODO Enable StreamingContext checkpointing

    val kafkaParams = Map(
      "metadata.broker.list" -> "192.168.0.50:9092",
      "schema.registry.url" -> "http://192.168.0.50:8081")

    val topicSet = Set("clicklog")

    val messages = KafkaUtils.createDirectStream[Object, Object, KafkaAvroDecoder, KafkaAvroDecoder](ssc, kafkaParams, topicSet).map(_._2)

    val windowedDStream = messages.cache().window(Minutes(10), Seconds(10))

    try {
      windowedDStream.foreachRDD(rdd => {
        val sqlContext = new SQLContext(sc)
        import sqlContext.implicits._

        // Parse AVro file and map it to ClickLog object
        val input = rdd.map {
          (parseAvro())
        }.toDF()

        // get ten most searched destinations within the last 10 minutes 
        val groupedDF = input.select("destination")
          .groupBy("destination")
          .count
          .orderBy(desc("count"))
          //.head(10) // Take top 10 destinations
          .show // TODO Remove - for testing purpose
      })
    } catch {
      case t: Throwable => t.printStackTrace() // TODO: handle error
    }

    ssc.start()
    ssc.awaitTermination()
  }

  def parseAvro(): Object => ClickLog = {
    avroRecord =>
      val data = avroRecord.asInstanceOf[GenericRecord]
      ClickLog(data.get("user_id").toString.trim.toInt, data.get("time").toString.trim, data.get("action").toString.trim, data.get("destination").toString.trim, data.get("hotel").toString.trim)
  }
}
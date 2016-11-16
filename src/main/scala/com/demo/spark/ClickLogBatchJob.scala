package com.demo.spark

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.desc
import org.apache.spark.sql.functions.from_unixtime
import org.apache.spark.sql.functions.rank
import org.joda.time.LocalDate

object ClickLogBatchJob {
  def main(args: Array[String]): Unit = {

    val sparkSession = SparkSession.builder
      .appName("ClickLogBatchJob")
      .getOrCreate()

    sparkSession.sparkContext.setLogLevel("ERROR")

    val input = sparkSession.read.parquet("/user/vagrant/parquetresults/")
    val inputDF = input.withColumn("eventDt", from_unixtime(input("time"), "YYYY-MM-dd")) // Parse unix timestamp to date

    // each user of the last day returns the second (distinct) hotel that they clicked on, 
    // and a null value for all users that did not click on two hotels.
    val outputDF1 = inputDF
      .filter(inputDF("eventDt").contains(new LocalDate().minusDays(1).toString())) // Filter clicklogs of last day  
      .filter(inputDF("action").contains("clicked"))
      .groupBy(inputDF("user_id"), inputDF("hotel")) // Group by users and hotels
      .count // Aggregate distinct hotels
      .withColumnRenamed("count", "clicked")

    val window = Window.partitionBy("user_id").orderBy(desc("clicked"), desc("hotel"))

    val outputDF2 = outputDF1
      .select(outputDF1("user_id"), outputDF1("hotel"), rank.over(window).alias("rank"))

    outputDF2.createOrReplaceTempView("refinedClickLogs")

    // Users who clicked second (distinct) hotel
    val outputDF3 = outputDF2
      .filter("rank =2")

    // Users who have not clicked second hotel
    val outputDF4 = sparkSession
      .sql("SELECT user_id,NULL,NULL FROM refinedClickLogs WHERE user_id NOT IN (select user_id from refinedClickLogs where rank > 1)")
      .toDF()

    val outputDF5 = outputDF3
      .unionAll(outputDF4)
      .sort("user_id")
      .drop("rank")
      .drop("clicked")
      .show
  }
}
# Project Documentation
###### 1) Explain which messaging system you would use and why. 

 * My first choice would be go with Kafka as It is built to be fault-tolerant, high-throughput, horizontally scalable, and allows geographically distributing data streams and processing
* Multiple consumer can work in parallel
* Easy to maintain and scale. It is based on distributed log approach, therefor it was be used for different usecases (messaging, website tracking, Database change, streaming processing...etc)
 
###### 2) What format would you use for the messages and why? (e.g. json/avro/protobuf)

__Avro__
* It has a direct mapping to and from JSON
* It has a rich, extensible schema language defined in pure JSON
* It has a very compact format. The bulk of JSON, repeating every field name with every single record, is what makes JSON inefficient for high-volume usage
* It has great bindings for a wide variety of programming languages so you can generate Java objects that make working with event data easier
* It has the best notion of compatibility for evolving your data over time
* Easy to maintain the schema registry with Confluent-io schema registry service

__Parquet__ 
* Store the events into HDFS as Parquet file for historical or batch analytics. 
* Parquet file format is based on columnar storage and it is available to any project in hadoop ecosystem.
* Efficiently encode nested structures and sparsely populated data based on the Google Dremel definition/repetition levels Provide extensible support for per-column encodings (e.g. delta, run length, etc)
* Provide extensibility of storing multiple types of data in column data (e.g. indexes, bloom filters, statistics)
* Offer better write performance by storing metadata at the end of the file
 
###### 3) What would you use to process these messages in near real time and why? (e.g. Storm, Spark or Samza)

__Spark__
* Spark Streaming however combines both where it treats streaming computations as a series of deterministic batch computations on small time intervals  
 
###### 4) With the message system, format and stream processor of your choice, write a process that returns the ten most searched destinations within the last 10 minutes.

__ClickLogStreamingToHDFS__
* Stream the data into HDFS as parquet file for historical analysis
* Batch interval of 1 second and this could be adjusted based on the event load
* Makes use of confluent-io schema registry to read the avro schema 

__ClickLogStreamingJob:__
* Creates direct stream from Kafka with the batch interval of 1 minutes and window for 10 minutes to find the top 10 searched hotels in last 10 minutes
* Uses AvroDeserializer
 
 
__ClickLogEventProducer:__ 
* Java program to send sample Avro payload to Kafka for testing the end to end pipeline
* Uses Kafka client API to send message to Kafka
* Makes use of Confluent-io schema registry to register avro schema to be used by consumers 
* Very flexible to maintain the schema changes in schema registry
 
###### 5) Write a process that for each user of the last day returns the second (distinct) hotel that they clicked on, and a null value for all users that did not click on two hotels.

__ClickLogBatchJob:__ 
* Batch spark job to statistics on historical data not on streaming data
* Loads parquet files from HDFS 

#### Things to Improve
- [ ] Better exception handling
- [ ] Checkpointing streaming context and replaying to events in case of failure
- [ ] Merge small parquet files and partition the files by year, month and day for historical anaysis
  
#### Project Requirements
* Kafka along with confluent-io schema registry 
* Requires Spark 2.0 binaries setup
* Requires HDFS setup
* I use my own custom built SMACK sandbox to develop and test (https://github.com/kartik-dev/SMACK-Sandbox)
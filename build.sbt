name := "ClickLogAnalytics"

version := "1.0"

val sparkVersion = "2.0.0"

scalaVersion := "2.11.7"

libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion  % "provided"

libraryDependencies += "org.apache.spark" %% "spark-streaming" % sparkVersion  % "provided"

libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-8" % sparkVersion

libraryDependencies  += "org.apache.spark" % "spark-sql_2.11" % sparkVersion % "provided"

libraryDependencies  += "org.apache.avro" % "avro" % "1.7.6" % "provided"

libraryDependencies  += "org.codehaus.jackson" % "jackson-mapper-asl" % "1.9.13"

libraryDependencies  += "com.google.code.gson" % "gson" % "2.3"

libraryDependencies += "io.confluent" % "kafka-avro-serializer" % "3.0.0"

libraryDependencies += "com.databricks" %% "spark-avro" % "2.0.1"

resolvers ++= Seq(
  Resolver.sonatypeRepo("public"),
  "Confluent Maven Repo" at "http://packages.confluent.io/maven/"
)

libraryDependencies ++= Seq ("joda-time" % "joda-time" % "2.8.2","org.joda" % "joda-convert" % "1.7")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList("org","apache","spark","unused","UnusedStubClass.class") => MergeStrategy.first
  case x => (assemblyMergeStrategy in assembly).value(x)
}
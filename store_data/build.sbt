name := "DroneDataStorage"

version := "0.1"

scalaVersion := "2.13.10"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.5.1",
  "org.apache.spark" %% "spark-sql" % "3.5.1",
  "org.apache.spark" %% "spark-streaming" % "3.5.1",
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % "3.5.1",
  "org.apache.hadoop" % "hadoop-common" % "2.7.3",
  "org.apache.hadoop" % "hadoop-hdfs" % "2.7.3",
  "org.apache.kafka" %% "kafka" % "2.8.0",
  "org.apache.kafka" % "kafka-clients" % "2.8.0",
  "org.apache.kafka" %% "kafka-streams-scala" % "2.8.0",
  "com.typesafe" % "config" % "1.4.1",
  "org.scalatest" %% "scalatest" % "3.2.10" % Test
)

resolvers ++= Seq(
  "Confluent" at "https://packages.confluent.io/maven/",
  "Apache Repo" at "https://repo1.maven.org/maven2/"
)
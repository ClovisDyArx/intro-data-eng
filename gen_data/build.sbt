name := "DroneDataProducer"

version := "0.1"

scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
  "org.apache.kafka" %% "kafka" % "2.8.0",
  "org.apache.kafka" % "kafka-clients" % "2.8.0",
  "org.apache.kafka" %% "kafka-streams-scala" % "2.8.0",
  "org.scalatest" %% "scalatest" % "3.2.10" % Test,
)

//libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime // pour avoir des logs très détaillés.



resolvers ++= Seq(
  "Confluent" at "https://packages.confluent.io/maven/",
  "Apache Repo" at "https://repo1.maven.org/maven2/"
)

mainClass in Compile := Some("DroneDataProducer")


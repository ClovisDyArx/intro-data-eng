name := "DroneDataProducer"

version := "0.1"

scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  "org.apache.kafka" %% "kafka" % "2.8.0",
  "org.apache.kafka" %% "kafka-clients" % "2.8.0"
)

mainClass in (Compile, run) := Some("DroneDataProducer")


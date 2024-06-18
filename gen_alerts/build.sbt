name := "AlertConsumer"

version := "0.1"

scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
  "org.apache.kafka" %% "kafka" % "2.8.0",
  "org.apache.kafka" % "kafka-clients" % "2.8.0",
  "org.apache.kafka" %% "kafka-streams-scala" % "2.8.0",
  "io.circe" %% "circe-core" % "0.14.1",
  "io.circe" %% "circe-generic" % "0.14.1",
  "io.circe" %% "circe-parser" % "0.14.1",
)

resolvers ++= Seq(
  "Confluent" at "https://packages.confluent.io/maven/",
  "Apache Repo" at "https://repo1.maven.org/maven2/"
)

mainClass in Compile := Some("AlertConsumer")

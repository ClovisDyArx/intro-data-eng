import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import java.time.LocalDate
import com.typesafe.config.ConfigFactory
import java.nio.file.Paths

//TODO : find why it can't find it from the other file..
case class DroneInfo(
  id: String,
  created: LocalDate,
  latitude: Float,
  longitude: Float,
  event_type: String,
  danger_level: Int,
  survivors: Int
)

object DroneDataProcessor {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load("application.conf")

    val sparkConf = new SparkConf()
      .setAppName("DroneDataProcessor")
      .setIfMissing("spark.master", "local[*]")

    val spark = SparkSession.builder.config(sparkConf).getOrCreate()
    val ssc = new StreamingContext(spark.sparkContext, Seconds(5))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> config.getString("kafka.bootstrap.servers"),
      "key.deserializer" -> classOf[org.apache.kafka.common.serialization.StringDeserializer],
      "value.deserializer" -> classOf[org.apache.kafka.common.serialization.StringDeserializer],
      "group.id" -> config.getString("kafka.group.id"),
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array(config.getString("kafka.topic"))

    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
    )

    stream.foreachRDD { rdd =>
      if (!rdd.isEmpty()) {
        val droneMessages = rdd.map(record => formatDroneMessage(record.value()))

        import spark.implicits._
        val droneDF = droneMessages.toDF()

        droneDF.write.mode(SaveMode.Append).parquet(Paths.get("./data/output").toAbsolutePath.toString())
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }

  def formatDroneMessage(value: String): DroneInfo = {
    import org.json4s._
    import org.json4s.jackson.JsonMethods._

    implicit val formats = DefaultFormats
    val parsed = parse(value)

    DroneInfo(
      id = (parsed \ "id").extract[String],
      created = LocalDate.parse((parsed \ "created").extract[String]),
      latitude = (parsed \ "latitude").extract[Float],
      longitude = (parsed \ "longitude").extract[Float],
      event_type = (parsed \ "event_type").extract[String],
      danger_level = (parsed \ "danger_level").extract[Int],
      survivors = (parsed \ "survivors").extract[Int]
    )
  }
}

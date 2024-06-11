import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object DroneDataProducer extends App {
  val bootstrapServers = sys.env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
  val topic = "quickstart-events"
  val interval = 1000

  val props = new Properties()
  props.put("bootstrap.servers", bootstrapServers)
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

  val producer = new KafkaProducer[String, String](props)

  while (true) {
    val droneInfo = DroneDataGenerator.generateDroneInfo()
    val record = new ProducerRecord[String, String](topic, droneInfo.id, droneInfo.toJsonString)
    producer.send(record)
    println(s"Sent: $droneInfo")
    Thread.sleep(interval) // toutes les 'interval' ms.
  }
  producer.close() // generation infinie

  implicit class DroneInfoJson(droneInfo: DroneInfo) {
    def toJsonString: String = {
      s"""
         |{
         | "id": "${droneInfo.id}",
         | "created": "${droneInfo.created}",
         | "latitude": ${droneInfo.latitude},
         | "longitude": ${droneInfo.longitude},
         | "event_type": "${droneInfo.event_type}",
         | "danger_level": ${droneInfo.danger_level},
         | "survivors": ${droneInfo.survivors}
         |}
       """.stripMargin
    }
  }
}

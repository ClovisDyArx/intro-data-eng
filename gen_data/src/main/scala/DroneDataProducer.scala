import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object DroneDataProducer extends App {
  val (config, topic) = ProducerConfig.getConfig("kafka-intro.conf")

  val producer = new KafkaProducer[String, String](config)

  val interval = 1000 // todo: adapter l'intervalle de temps entre les générations de données.

  while (true) {
    val droneInfo = DroneDataGenerator.generateDroneInfo()
    val record = new ProducerRecord[String, String](topic, droneInfo.id, droneInfo.toJsonString)

    producer.send(record)
    println(s"Sent: $droneInfo")

    Thread.sleep(interval)
  }

  producer.close()
}

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


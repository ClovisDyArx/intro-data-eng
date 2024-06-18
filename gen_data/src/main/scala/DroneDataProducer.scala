import java.util.Properties
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._ // NE PAS TOUCHER : si on retire ça, ça envoie rien ??????


object DroneDataProducer extends App {
  val bootstrapServers = sys.env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")
  val topic = "quickstart-events"
  val interval = 1000 // useless maintenant
  val batchSize = 500 // envoie 500 messages.

  val props = new Properties()
  props.put("bootstrap.servers", bootstrapServers)
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

  val producer = new KafkaProducer[String, String](props)

  def generateAndSendBatch(): Future[Unit] = {
    val batch = List.fill(batchSize)(DroneDataGenerator.generateDroneInfo())

    val records = batch.map { droneInfo =>
      new ProducerRecord[String, String](topic, droneInfo.id, droneInfo.toJsonString)
    }

    val futures = records.map { record =>
      Future {
        producer.send(record)
        println(s"Sent: ${record.value()}") // debug: afficher les messages envoyés
      }
    }

    Future.sequence(futures).map(_ => ())
  }

  def scheduleSendBatches(): Future[Unit] = {
    Future {
      generateAndSendBatch()
      // Thread.sleep(interval) // aucun effet sur le nouveau code, donc useless
    }.flatMap { _ =>
      scheduleSendBatches()
    }
  }

  scheduleSendBatches()

  sys.addShutdownHook {
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
}

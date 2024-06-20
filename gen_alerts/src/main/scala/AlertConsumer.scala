import java.util.Properties
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import io.circe.parser.decode
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps

import scala.jdk.CollectionConverters._

object AlertConsumer{

  def main(args: Array[String]): Unit = {
    val topic: String = "quickstart-events"

    val bootstrapServers: String = sys.env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")

    val consumerProps: Properties = new Properties()
    consumerProps.put("bootstrap.servers", bootstrapServers)
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "alert-consumer-group")
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])

    val producerProps: Properties = new Properties()
    producerProps.put("bootstrap.servers", bootstrapServers)
    producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
    producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])

    val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](consumerProps)
    val producer: KafkaProducer[String, String] = new KafkaProducer[String, String](producerProps)

    consumer.subscribe(List(topic).asJava)

    processRecords(consumer, producer)
  }

  def processRecords(consumer: KafkaConsumer[String, String], producer: KafkaProducer[String, String]): Unit = {
    LazyList.continually {
      consumer.poll(java.time.Duration.ofMillis(1000))
        .asScala
        .foreach(record => handleData(record.value(), producer))
      }
      .foreach(identity)
    }

  private def handleData(data: String, producer: KafkaProducer[String, String]): Unit = {
    decode[DroneInfo](data) match {
      case Right(droneInfo: DroneInfo) =>
        processDroneData(droneInfo).foreach { alert =>
          println(s"ALERT: ${alert.message}")
          val alertJson: String = alert.asJson.noSpaces
          producer.send(new ProducerRecord[String, String]("alert-notifications", alertJson))
        }
      case Left(error) =>
        println(s"Failed to decode data: $error")
    }
  }

  def processDroneData(droneData: DroneInfo): Option[AlertMessage] = {
    if (droneData.danger_level > 2 && droneData.survivors > 0) {
      Some(AlertMessage(
        droneData.created,
        droneData.danger_level,
        droneData.survivors,
        droneData.latitude,
        droneData.longitude,
        s"Drone detected an event at ${droneData.created}, coordinates ${droneData.latitude}, ${droneData.longitude} with danger level ${droneData.danger_level} and ${droneData.survivors} survivors."
      ))
    } else {
      None
    }
  }
}

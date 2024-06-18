import java.util.Properties
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.serialization.StringDeserializer
import io.circe.parser.decode
import io.circe.generic.auto._
import scala.jdk.CollectionConverters._
import javax.mail.{Message, Session, Transport}
import javax.mail.internet.{InternetAddress, MimeMessage}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object NotificationConsumer {

  private val emailDestination :String = "alertes.drones@gmail.com"

  def main(args: Array[String]): Unit = {
    val consumerProps = new Properties()
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-consumer-group")
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])

    val consumer = new KafkaConsumer[String, String](consumerProps)
    consumer.subscribe(List("alert-notifications").asJava)

    processRecords(consumer)
  }

  def processRecords(consumer: KafkaConsumer[String, String]): Unit = {
    consumer.poll(java.time.Duration.ofMillis(1000))
      .asScala
      .foreach(record => handleAlert(record.value()))
  }

  def handleAlert(data: String): Unit = {
    decode[AlertMessage](data) match {
      case Right(alert) =>
        println(s"Processing alert: ${alert.message}")
        sendEmail(emailDestination, "Drone Alert", alert.message)
      case Left(error) =>
        println(s"Failed to decode alert message: $error")
    }
  }

  def sendEmail(to: String, subject: String, body: String): Unit = {
    val emailConfig: EmailConfig = ConfigSource.default.loadOrThrow[EmailConfig]

    val props = new Properties()
    props.put("mail.smtp.auth", "true")
    props.put("mail.smtp.starttls.enable", "true")
    props.put("mail.smtp.host", "smtp.gmail.com")
    props.put("mail.smtp.port", "587")
    props.put("mail.smtp.ssl.protocols", "TLSv1.2")

    val session = Session.getInstance(props, new javax.mail.Authenticator {
      override protected def getPasswordAuthentication: javax.mail.PasswordAuthentication = {
        new javax.mail.PasswordAuthentication(emailConfig.username, emailConfig.password)
      }
    })

    try {
      val message = new MimeMessage(session)
      message.setFrom(new InternetAddress(emailConfig.username))
      message.setRecipient(Message.RecipientType.TO, new InternetAddress(to))
      message.setSubject(subject)
      message.setText(body)

      Transport.send(message)
      println(s"Email sent successfully to $to")
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }
}


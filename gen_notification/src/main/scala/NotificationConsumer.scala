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
import io.github.cdimascio.dotenv.Dotenv
import com.typesafe.config.{Config, ConfigFactory}

object NotificationConsumer {
  //Utilisation des variables d'environnement
  private val dotenv = Dotenv.load()

  private val config = ConfigFactory.load()

  private val configWithEnv = ConfigFactory.parseString(
    config
      .root()
      .render()
      .replace("REPLACE_WITH_USER", dotenv.get("MAIL_USERNAME"))
      .replace("REPLACE_WITH_PASS", dotenv.get("MAIL_PASSWORD"))
  ).withFallback(config)

  private val emailConfig: EmailConfig = ConfigSource.fromConfig(configWithEnv).loadOrThrow[EmailConfig]

  private val propsMails = new Properties()
  propsMails.put("mail.smtp.auth", "true")
  propsMails.put("mail.smtp.starttls.enable", "true")
  propsMails.put("mail.smtp.host", "smtp.gmail.com")
  propsMails.put("mail.smtp.port", "587")
  propsMails.put("mail.smtp.ssl.protocols", "TLSv1.2")

  private val session = Session.getInstance(propsMails, new javax.mail.Authenticator {
    override protected def getPasswordAuthentication: javax.mail.PasswordAuthentication = {
      new javax.mail.PasswordAuthentication(emailConfig.username, emailConfig.password)
    }
  })

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
    val batchSize = 100
    val delayBetweenBatches = 10000

    LazyList.continually {
      consumer.poll(java.time.Duration.ofMillis(1000))
        .asScala
        .toList
        .grouped(batchSize)
        .foreach { batch =>
        batch.foreach { record =>
          handleAlert(record.value())
        }

        println(s"Processed batch of size ${batch.size}. Waiting for next batch.")
        Thread.sleep(delayBetweenBatches)
      }
    }.foreach(identity)
  }

  def handleAlert(data: String): Unit = {
    decode[AlertMessage](data) match {
      case Right(alert) =>
        println(s"Processing alert: ${alert.message}")
        /*Seulement si le batch est petit. Nous avons une limite du nombre d'envoie possible avec gmail.
          Le problème peut être résolu en utilisant des api faites pour ça comme SendGrid par exemple mais
          nous ne sommes pas une entreprise.*/
        //sendEmail(emailDestination, "Drone Alert", alert.message)
      case Left(error) =>
        println(s"Failed to decode alert message: $error")
    }
  }

  def sendEmail(to: String, subject: String, body: String): Unit = {

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


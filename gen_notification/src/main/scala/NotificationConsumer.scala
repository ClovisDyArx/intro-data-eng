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

object NotificationConsumer extends App {
  //Utilisation des variables d'environnement
  val dotenv = Dotenv.load()
  val config = ConfigFactory.load()
  val configWithEnv = ConfigFactory.parseString(
    config
      .root()
      .render()
      .replace("REPLACE_WITH_USER", dotenv.get("MAIL_USERNAME"))
      .replace("REPLACE_WITH_PASS", dotenv.get("MAIL_PASSWORD"))
  ).withFallback(config)
  val emailConfig: EmailConfig = ConfigSource.fromConfig(configWithEnv).loadOrThrow[EmailConfig]

  //initialisation de la session de mails
  val propsMails: Properties = new Properties()
  propsMails.put("mail.smtp.auth", "true")
  propsMails.put("mail.smtp.starttls.enable", "true")
  propsMails.put("mail.smtp.host", "smtp.gmail.com")
  propsMails.put("mail.smtp.port", "587")
  propsMails.put("mail.smtp.ssl.protocols", "TLSv1.2")
  val session: Session = Session.getInstance(propsMails, new javax.mail.Authenticator {
    override protected def getPasswordAuthentication: javax.mail.PasswordAuthentication = {
      new javax.mail.PasswordAuthentication(emailConfig.username, emailConfig.password)
    }
  })

  //destinataire
  val emailDestination: String = "alertes.drones@gmail.com"

  val topic = "alert-notifications"

  val bootstrapServers: String = sys.env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092")

  val consumerProps: Properties = new Properties()
  consumerProps.put("bootstrap.servers", bootstrapServers)
  consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-consumer-group")
  consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
  consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
  consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](consumerProps)
  consumer.subscribe(List(topic).asJava)

  processRecords(consumer)

  //Récupération des alertes par batch (nécessaire pour gérer l'envoie des mails)
  def processRecords(consumer: KafkaConsumer[String, String]): Unit = {
    LazyList.continually {
        consumer.poll(java.time.Duration.ofMillis(1000))
          .asScala
          .foreach(record => handleAlert(record.value()))
      }
      .foreach(identity)
  }

  def handleAlert(data: String): Unit = {
    decode[AlertMessage](data) match {
      case Right(alert: AlertMessage) =>
        println(s"Processing alert: ${alert.message}")
        /*Seulement si le batch est petit. Nous avons une limite du nombre d'envoie possible avec gmail.
          Le problème peut être résolu en utilisant des api faites pour ça comme SendGrid par exemple mais
          nous ne sommes pas une entreprise.*/
        sendEmail(emailDestination, "Drone Alert", alert.message)
      case Left(error) =>
        println(s"Failed to decode alert message: $error")
    }
  }

  //Envoie des mails
  def sendEmail(to: String, subject: String, body: String): Unit = {

    try {
      val message: MimeMessage = new MimeMessage(session)
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


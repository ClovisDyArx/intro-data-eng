import java.time.LocalDateTime

case class AlertMessage(
                         timestamp: LocalDateTime,
                         dangerLevel: Int,
                         survivors: Int,
                         latitude: Float,
                         longitude: Float,
                         message: String
                       )

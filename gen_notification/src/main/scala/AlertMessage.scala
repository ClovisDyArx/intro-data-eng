import java.time.LocalDate

case class AlertMessage(
                         timestamp: LocalDate,
                         dangerLevel: Int,
                         survivors: Int,
                         latitude: Float,
                         longitude: Float,
                         message: String
                       )

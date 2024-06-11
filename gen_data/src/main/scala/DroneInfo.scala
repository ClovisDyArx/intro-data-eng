import java.time.LocalDate

case class DroneInfo(
  id: String,
  created: LocalDate,
  latitude: Float,
  longitude: Float,
  event_type: String,
  danger_level: Int,
  survivors: Int
)


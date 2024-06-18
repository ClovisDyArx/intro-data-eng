import java.time.{LocalDateTime}

case class DroneInfo(
  id: String,
  created: LocalDateTime,
  latitude: Float,
  longitude: Float,
  event_type: String,
  danger_level: Int,
  survivors: Int
)


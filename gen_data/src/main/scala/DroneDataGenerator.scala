import java.time.{LocalDateTime}
import scala.util.Random

object DroneDataGenerator {
  private val eventTypes = Array("earthquake", "tsunami", "wildfire", "flood", "attack", "landslide", "tornado", "drought", "cyclone", "avalanche")

  def generateDroneInfo(): DroneInfo = {
    DroneInfo(
      id = Random.alphanumeric.take(10).mkString, //ex: S69E69X69Y
      created = LocalDateTime.now(), // timestamp
      latitude = Random.between(-90.0f, 90.0f), // [-90.0, 90.0]
      longitude = Random.between(-180.0f, 180.0f), // [-180.0, 180.0]
      event_type = eventTypes(Random.nextInt(eventTypes.length)), // ex: tsunami
      danger_level = Random.nextInt(6), // [0, 5]
      survivors = Random.nextInt(21) // [0, 20]
    )
  }
}


# **Source**

https://www.baeldung.com/scala/kafka

# **Fichiers Drones**
## **DroneInfo.scala**

Fichier contenant la déclaration de la classe utilisée pour la génération de données:

case class DroneInfo( \
  id: String, // ex: 6969696969 \
  created: LocalDate, // ex: 2024-06-11 \
  latitude: Float, // [-90, 90] \
  longitude: Float, // [-180, 180] \
  event\_type: String, // ex: earthquake, tsunami, .. \
  danger\_level: Int // [0, 5] \
  survivors: Int // [0, x < inf] \
) \

## **DroneDataGenerator.scala**

Fichier contenant l'instanciation et génération aléatoire de données pour les objets DroneInfo.


## **DroneDataProducer.scala**

Fichier permettant l'envoie de donnée drone générée dans une stream kafka.

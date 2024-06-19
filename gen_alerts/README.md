# **Fichiers Alertes**
## **AlertConsumer.scala**
Fichier permettant la lecture et le traitement des données de la stream contenant les informations des drones.

Ce fichier genère également des alertes et les envoie dans une nouvelle stream pour la création des notifications.

## **AlertMessage.scala**

Fichier contenant la déclaration de la classe utilisée pour la génération d'alertes:

case class AlertMessage( \
timestamp: LocalDateTime, // ex: 2024-06-19T10:53:26.819089 \
dangerLevel: Int, // [0, 5] \
survivors: Int, // [0, x < inf]\
latitude: Float, // [-90, 90] \
longitude: Float, // [-180, 180] \
message: String // ex: Drone detected an event at 2024-06-19T10:53:26.819162, coordinates -43.040367, 72.96771 with danger level 3 and 3 survivors. \
)

## **DroneInfo**

Fichier contenant la déclaration de la classe utilisée pour la récupération des données:

case class DroneInfo( \
id: String, // ex: 6969696969 \
created: LocalDateTime, // ex: 2024-06-19T10:53:26.819089 \
latitude: Float, // [-90, 90] \
longitude: Float, // [-180, 180] \
event\_type: String, // ex: earthquake, tsunami, .. \
danger\_level: Int // [0, 5] \
survivors: Int // [0, x < inf] \
) \
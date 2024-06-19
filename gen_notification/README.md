# **Fichiers Notifications**
## **NotificationConsumer.scala**
Fichier permettant la lecture et le traitement des données de la stream contenant les alertes.

Ce fichier peut également générer des mails si la ligne est décommentée (celle-ci n'est pas toujours mise à cause de la limite de mails pouvant être envoyé par jour).

## **AlertMessage.scala**

Fichier contenant la déclaration de la classe utilisée pour la récupération d'alertes:

case class AlertMessage( \
timestamp: LocalDateTime, // ex: 2024-06-19T10:53:26.819089 \
dangerLevel: Int, // [0, 5] \
survivors: Int, // [0, x < inf]\
latitude: Float, // [-90, 90] \
longitude: Float, // [-180, 180] \
message: String // ex: Drone detected an event at 2024-06-19T10:53:26.819162, coordinates -43.040367, 72.96771 with danger level 3 and 3 survivors. \
)

## **EmailConfig.scala**

Fichier contenant la déclaration de la classe utilisée pour la récupération des informations du mail envoyant les alertes:

case class EmailConfig(\
username: String,\
password: String\
)

Les informations sur l'email sont renseignées dans application.conf.

Un .env à la racine de ce projet (racine de gen_notification) est nécessaire au bon fonctionnement des envoies de mail avec les informations suivantes:
MAIL_USERNAME="votre.mail@mail.com"
MAIL_PASSWORD="votreMDP ou MDP d'application"
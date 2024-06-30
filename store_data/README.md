# **Fichiers Stockage**
## **DroneDataProcessor.scala**

Fichier permettant la lecture et le traitement des données du stream contenant les informations des drones.  
Les données seront ensuite stockées localement dans un dossier `data/output`, sous la forme de fichiers .snappy.parquet.

## storage.conf

Fichier contenant les informations nécessaires à la configuration de kafka. Dans le cas de l'utilisation d'un cluster hdfs par exemple, les informations le concernant pourraient être ajoutées dans ce fichier et le code ne nécessiterait que très peu de modifications.
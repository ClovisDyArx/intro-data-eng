# README

## Preliminary questions

1)	**What technical/business constraints should the data storage component of the program architecture meet to fulfill the requirement described by the customer in paragraph «Statistics» ? So what kind of component(s) (listed in the lecture) will the architecture need?**

Contraintes: La base de données doit pouvoir supporter beaucoup d'opérations d'ajout de données tout au long de la journée. Par la suite, on doit pouvoir trier et récupérer facilement et rapidement les données liées à une position géographique. La base de données doit pouvoir garder beaucoup de données sur le long terme.  
Types de composants : processing et storage (No-SQL document (mangoDB))

2)	**What business constraint should the architecture meet to fulfill the requirement describe in the paragraph «Alert»? Which component to choose?**

Contraintes:  L'alerte doit être faite dans les plus brefs délais. L'architecture doit donc avoir un moyen de récupérer toutes les données de tous les drones, les traiter rapidement et notifier le service approprié en temps réel.  
Composants choisis : Stream pour récupérer les données et une API pour les traiter

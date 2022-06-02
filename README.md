# ULP OBSERVABILITY PLUGIN

Ulp observability plugin est une extension pour Jmeter qui va permettre d'afficher des metriques plus détaillées sur des tests de charge (implémentées et exécutées par Jmeter)

## Les métriques à afficher:
- Nom du Sampler utilisé pour le test de charge
- Nombre de requêtes
- % Erreur
- Temps moyen
- Percentiles 1 (propriété aggregate_rpt_pct1)
- Percentiles 2 (propriété aggregate_rpt_pct2)
- Percentiles 3 (propriété aggregate_rpt_pct3)
- Temps max
- Débit en req/s


## Contraintes:

- Voir un impact mémoire / CPU le plus léger possible côté JMeter
- Pouvoir gérer jusqu'à 1 million de requêtes par minute
- Sélectionner une libraire Javascript de graphing gratuite et compatible Open Source ou payante mais en One Shot seulement
- Développer la solution en Plugin
- Ne doit pas bloquer l'appelant
- Utiliser un serveur léger en termes de consommation mémoire/ CPU (Embedded Jetty)
- Code compatible Java 11


## Technologies et dépendances:
##### Back
- [Java](https://www.java.com/) : Language utilisé par Jmeter
- [Embedded Jetty](https://www.baeldung.com/jetty-embedded) : pour exposer les metrics récoltés par SamplerListener (propre à Jmeter) et calculés par HDRHistogram offre une implémentation de serveurs très légère
- [Micrometer](https://micrometer.io/) : pour enregistrer des échantillons et calculer d'avantages de métrics
- [CronScheduler](https://github.com/TimeAndSpaceIO/CronScheduler/) : pour un planificateur de tâches insensible à la dérive d'horloge
- [openapi-generator-maven-plugin](https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-maven-plugin) : pour générer les APIs et modèles
- [Jackson](https://github.com/FasterXML/jackson) : pour la sérialisation des réponses d'API
- [Junit](https://www.jmdoudoux.fr/java/dej/chap-junit.htm) : Tests Unitaires


###### Tâches accomplies
- Traitement des samples multithread
- Configuration personnalisée de sampler
- Journalisation de métriques
- Exposition des métriques au format OpenMetrics avec le serveur Jetty
- Exposition d'une page HTML avec des graphiques de métriques
- Javadoc

###### IHM

<p align="center">
<img src=screenshot/ulp_observability1.png><br/>
<em>Panneau de configuration JMeter de listener ULP Observability</em> 
<br/>
<br/>
<img src=screenshot/ulp_observability2.png><br/>
<em>Exemple de résumé des métriques en mode non graphique</em>
<br/>
<br/>
<img src=screenshot/ulp_observability5.png><br/>
<em>Exemple de réponse du serveur Jetty pour les métriques d'un échantillon au format OpenMetrics</em>
</p>


##### Front
- [TypeScript](https://www.typescriptlang.org/) : pour profiter du typage pour plus de rigueur et cohérence de données 
- [AngularJs](https://reactjs.org/) : pour la partie front et graphing : 
        - Des states et hooks pour faciliter la manipulation de données (Métriques)
        - Répo npm pour diverses dépendances
        - ChartJs disponible
        - Material UI pour un rendu élégant
- [ChartJs](https://www.npmjs.com/package/chart.js?activeTab=readme) : Librarie beaucoup utilisée, maintenu (dernière mise à jour le 16/02/2022) et gratuite.
- [ng-openapi-gen](https://www.npmjs.com/package/ng-openapi-gen) : générer les objets et les services
- [Jest](https://jestjs.io/) : Tests unitaires, populaire et préferé par la communauté react et angular

###### Tâches accomplies
- Synchronisation avec la configuration du plugin
- La page HTML avec des métriques affiche correctement des graphiques multi-axes pour chaque type de métrique
- Récapitulative des métriques totales en bas de la page

###### IHM

<p align="center">
<img src=screenshot/ulp_observability3.png><br/>
<em>Exemple d'un graphique pour une métrique</em> <br />
</p>

- Axe y gauche : métrique de chaque groupe d'échantillons
- Axe y droite : nombre cumulé des threads de chaque groupe d'échantillons
- Graphiques actuellement mis en oeuvre pour : réponse moyenne, réponse maximale, centiles, pourcentage d'erreur et débit

<br />
<p align="center">
<img src=screenshot/ulp_observability4.png><br/>
<em>Exemple de résumé des métriques totales</em> <br />
</p>

###### A faire
- La sérialisation/désérialisation des enregistrements d'échantillons dans un fichier local
- Appliquer checkstyle pour la partie back
- Créer Action github de build du plugin
- Faire le packaging du plugin approprié (`mvn clean install` depuis le projet parent génèrant un JAR de plugin avec un webapp depuis front build)
- Tests unitaires
- Pour la partie front : ajouter un tableau des statistiques équivalent au tableau de bord JMeter (<https://jmeter.apache.org/usermanual/generating-dashboard.html>)
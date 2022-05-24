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
- [Prometheus?](https://prometheus.io/) : (PrometheusMeterRegistry)/(io.micrometer.core.instrument.Metrics) pour calculer d'avantages de métrics et les exposer en un endpoint qui va servir comme source de données pour le front (en cours d'étude)
- [HDRHistogram](http://hdrhistogram.org/) : pour calculer les percentilles
- [openapi-generator-maven-plugin](https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-maven-plugin) : pour générer les APIs et modèles
- [Junit](https://www.jmdoudoux.fr/java/dej/chap-junit.htm) : Tests Unitaires

###### Back IHM

<p align="center">
<img src=screenshot/ulp_observability1.png><br/>
<em>Panneau de configuration JMeter de listener ULP Observability</em> 
<br/>
<br/>
<img src=screenshot/ulp_observability2.png><br/>
<em>Exemple de résumé des métriques en mode non graphique</em>
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

###### Front IHM

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
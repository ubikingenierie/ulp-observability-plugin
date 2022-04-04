
# Etude choix de modèle et stockage des métriques ULPOBERVABILITY 

## 1) Etude format de données des métriques à exposer par l'endpoint 
### [Prometheus](https://prometheus.io/):
       
##### Métriques proposées:
- Counter
- Gauge
- Histogram
- Summary
- Untyped

##### [Format de données](https://docs.google.com/document/d/1ZjyKiKxZV83VI9ZKAXRGKaUKK2BIWCT7oiGBKDBpjEY/edit):
##### Format text
```
#: un commentaire
# HELP : at least one more token is expected, which is the metric name. All remaining tokens are considered the docstring for that metric name.
# TYPE : exactly two more tokens are expected. The first is the metric name, and the second is either counter, gauge, histogram, summary, or untyped, defining the type for the metric of that name

# HELP http_requests_total The total number of HTTP requests.
# TYPE http_requests_total counter
http_requests_total{method="post",code="200"} 1027 1395066363000
http_requests_total{method="post",code="400"}    3 1395066363000

# Escaping in label values:
msdos_file_access_time_seconds{path="C:\\DIR\\FILE.TXT",error="Cannot find file:\n\"FILE.TXT\""} 1.458255915e9
```
##### Format Json
```
[
  {
    "baseLabels":{
      "__name__":"instance_start_time_seconds"
    },
    "docstring":"The time at which the current instance started (UTC).",
    "metric":{
      "type":"gauge",
      "value":[
        {
          "labels":{

          },
          "value":1.366893324e+09
        }
      ]
    }
  }
  }
]
```

##### Avantages de prometheus:
- [Prometheus propose différents formats de données](https://prometheus.io/docs/instrumenting/exposition_formats/): Text, Json, Buffer et autre.
- La majorité des solutions de métriques acceptent le format text à Prometheus.


### [Open Metrics](https://github.com/OpenObservability/OpenMetrics/blob/main/specification/OpenMetrics.md):
OpenMetrics est un effort pour standardiser le format de métriques de Prometheus.
Le format d'exposition Prometheus et OpenMetrics sont essentiellement les mêmes, à l'exception de quelques améliorations (data types, métriques proposées, EOF pour détecter les fins de métriques...)

- Supporté par prometheus et DataDog.
- Prometheus Python client est la référence d'implémentation, et utilise le modèle de Data à OpenMetrics.
- Prometheus privilégie OpenMetrics avant autre format [video](https://www.youtube.com/watch?v=C-BJAzCiMyY&t=632s).

###### Types de donnée:
- Booleans
- Timestamps
- Strings
- Label
- LabelSet
- MetricPoint
- Exemplars

###### Métriques proposées:
- unknown
- gauge
- counter
- stateset
- info
- histogram
- gaugehistogram
- summary

##### Avantages de Openmetrics par rapport à Prometheus:
- Spécification beaucoup plus stricte du format, par exemple l'espacement, l'échappement.
- Autorisation des timestamps en nanoseconde. Valeurs Int64.
- Unité en tant que nouvelle métadonnée.
- _created pour la création d'enfants.
- EOF explicite pour détecter les métriques interrompues.
- Considérations pour le pull et le push.
- Supporte le format text et [protobufs](https://developers.google.com/protocol-buffers), tandis que prometheus supporte que le format text ( Les protobuf est le langage neutre à Google, qui permet de sérialiser des données structurées, on peut penser à XML, mais en plus petit, plus rapide et plus simple).
- Depuis la version 6.5.0, l’Agent DATAlog inclut des checks OpenMetrics et Prometheus capables de scraper les endpoints Prometheus. Il est recommendé d’utiliser le format OpenMetrics du fait de son efficacité et de sa prise en charge complète du format texte Prometheus. 

##### Transition à Openmetrics:
- Ajouter _total maintenant pour les compteurs.cela ne devrait pas poser de problème à ceux qui utilisent des bibliothèques clientes existantes.
- Si vous n'utilisez pas de bibliothèque client, assurez-vous que vous envoyez un fichier de type
Content-Type approprié si vous prévoyez de continuer à exposer le format texte de Prometheus.
- De même, si vous écrivez un scraper et souhaitez utiliser le format texte Prometheus (ou
OpenMetrics), définissez votre en-tête Accept en conséquence.


##### Exemple:
```
Prometheus:
#TYPE foo_seconds_total counter
foo_seconds_total 1.0
```
```
OpenMetrics (including optional UNIT and _created):
#TYPE foo_seconds counter
#UNIT foo_seconds seconds
foo_seconds_total 1.0
foo_seconds_created 1572628096.0
#EOF
```
## 2) Avantages et inconvénients du localstorage/database:
La visualisation des données est pendant la phase d'un tir, généralement le temps du tir est 2h à 8h.
les variables jouant sur la volumétrie du fichier sont : le nombre de samplers, le pas dans le temps et le détail de l'erreur.

```
nombre de ligne dans le fichier = nombre de samplers * pas (fréquence) dans le temps
```

###### Exemple:

```
 pour 5 samplers | 2 secondes de pas | 2h de tir (7200s) ça nous fera: 5*(7200/2) = 18000 lignes chaque 2h
```

##### Option 1: Local Storage du navigateur
Après un test sur un simple fichier txt, en stockant les métriques demandées pour avoir une taille de 5MO, voici les résultats (peut être Session Storage car on sera obligé de vider le local storage après le tir):
- 5000000 caractères ont été stockés, 41000 lignes, 118 caractères sur chaque ligne
- Si on suppose que chaque ligne représente un enregistrement sur une seconde, 41000 lignes <=> 12h de stockage
- Pour 2h de temps on peut stocker à peu près jusqu'à 5 lignes (5 appels) par secondes 

##### Option 2: Utiliser un fichier pour stocker les métriques:
###### Coté navigateur:
Pour contourner cette limitation du localstorage (5MO), créer un fichier en javascript peut être une solution, mais le javascript ne permet pas de faire ça car ça représente une grosse faille de sécurité.

###### Coté serveur:
Stocker les métriques dans un simple fichier JSON est fortement envisageable, une BDD pour un simple besoin de stocker et récupérer les métriques est susceptible de consommer des ressources supplémentaires inutilement.

##### Option 3: Utiliser une base de données à charger en mémoire (HSQLDB):
Pour contourner cette limitation du localstorage (5MO), on peut envisager une base de donnée chargée dans la mémoire, mais celà entrainera une charge et des coûts supplémentaires coté JMETER

La plupart des implémentations de la JVM allouent une quantité maximale de mémoire (généralement 64 Mo par défaut). Cette quantité n'est généralement pas suffisante lorsque de grandes tables de mémoire sont utilisées, ou lorsque la taille moyenne des lignes dans les tables mises en cache est supérieure à quelques centaines d'octets. La quantité maximale de mémoire allouée peut être définie sur la ligne de commande Java qui est utilisée pour exécuter HyperSQL. Par exemple, le paramètre JVM -Xmx256m augmente la quantité à 256 Mo.

###### Allocation de la mémoire dans le cache:

Avec les tables en cache, les données sont stockées sur le disque et seul un nombre maximum de lignes est conservé en mémoire à tout moment. La valeur par défaut est de 50 000 lignes maximum. La commande `SET FILES CACHE ROWS` ou la propriété de connexion hsqldb.cache_rows permet de modifier cette quantité. La quantité de mémoire requise par les lignes mises en cache peut atteindre la somme des lignes contenant les plus grandes données de champ. Par exemple, si une table de 100 000 lignes contient 40 000 lignes avec 1 000 octets de données dans chaque ligne et 60 000 lignes avec 100 octets dans chaque ligne, le cache peut s'agrandir pour contenir 50 000 des plus petites lignes, mais comme expliqué ci-dessous, seulement 10 000 des grandes lignes.

On peut envisager cette solution, si le nombre de tir est élevé, en réduisant le nombre de lignes maximal dans le cache (`SET FILES CACHE ROWS`) (car on fait que stocker et récupérer l'information donc on aura besoin de rollback et transactions qui occupent souvent de la mémoire dans le cache).

#### Liens vers les documentations
- [Micrometer Documentation](https://micrometer.io/docs)
- [HdrHistogram](https://github.com/HdrHistogram/HdrHistogram)
- [prometheus-metrics](https://sysdig.com/blog/prometheus-metrics/)
- [OpenMetrics: Is Prometheus unbound?](https://sysdig.com/blog/prometheus-metrics/)
- [pdf expliquant openmetrics](https://promcon.io/2019-munich/slides/openmetrics-what-does-it-mean-for-you.pdf)
- [HSQLDB](http://hsqldb.org/doc/2.0/guide/deployment-chapt.html): 



Bonjour Jughurta,
Pour l'instant avance sur le reste en mettant en dur les différents éléments.

Merci

On Wed, Mar 23, 2022 at 4:21 PM <j.kebir@ubik-ingenierie.com> wrote:
Bonjour Philippe,

le plugin s'affiche désormais sur la liste des listeners (capture ci-jointe), je me demande quels arguments à récupérer pour la configuration, j'ai pensé au n° port pour l'endpoint, peut-être les métriques à afficher aussi?

Merci,

Jugurtha 

Le 22.03.2022 18:25, Philippe Mouawad a écrit :

Bonjour,
 
Crée une branche develop et une master.
Tu fais une  PR par feature sur une branche forkée de la branche develop , Guillaume et moi faisons une revue et mergeons.
 
Merci

On Tue, Mar 22, 2022 at 2:36 PM <j.kebir@ubik-ingenierie.com> wrote:
Bonjour Philippe,

je viens de pusher la structure du projet (juste la structure).

Est-ce-que ça te va si je fais ainsi:

Pour chaque ticket je crée une branche
Les messages des commits en Anglais avec ces conventions :
feat: pour de nouvelles fonctionnalités
refactor: pour une mise à jour
fix: pour une correction d'erreur:
setup: pour une configuration ou une mise en place d'une architecture 
?

Merci bien,

Jugurtha

Le 22.03.2022 09:38, j.kebir@ubik-ingenierie.com a écrit :

Bonjour Philippe,

je viens de finir la création des tickets sur le github, j'avais fait le point avec Guillaume aussi.

Si t'as un peu de disponibilité pour jeter un coup d'oeil et voir si j'ai bien compris.

Merci bien,

Jugurtha

Le 21.03.2022 17:36, Philippe Mouawad a écrit :

Commence à créer les tickets github de ton côté dès maintenant pour identifier les différents blocs à développer.
Et demain tu échangeras avec Guillaume pour voir ce qui est à compléter.

Merci

On Mon, Mar 21, 2022 at 5:31 PM <j.kebir@ubik-ingenierie.com> wrote:
ça marche, c'est bien noté.

Je vais faire le point avec Guillaume s'il est disponible demain, si tout est ok pour toi aussi je peux commencer à étudier les plugins à Jmeter et commencer à développer.  

Merci bien,

Jugurtha

Le 21.03.2022 17:23, Philippe Mouawad a écrit :

Bonjour Jughurta,
Mes réponses ci-dessous.

Cordialement

On Mon, Mar 21, 2022 at 10:08 AM <j.kebir@ubik-ingenierie.com> wrote:
Bonjour Philippe, Guillaume,

si j'ai bien compris les variables jouant sur la volumétrie du fichier sont : le nombre de samplers, le pas dans le temps et le détail de l'erreur.

nombre de ligne dans le fichier = nombre de samplers * pas (fréquence) dans le temps

Par exemple: pour 5 samplers | 2 secondes de pas | 2h de tir (7200s) ça nous fera: 5*(7200/2) = 18000 lignes chaque 2h

Hors erreur c'est bien le calcul.
Mais si on veut tracer les erreurs , là le type d'erreur  (lié au libellé de l'erreur) entre en jeu , pour un même sampler Login par exemple on pourrait avoir:
OK 200
Erreur 500
Erreur 404
Read Timed out
Connect Timed out
Tous les status possibles (400, 401, 403)...
Parfois des libellés différents pour une erreur 500
C'est là que c'est plus compliqué de determiner le nombre. On met de côté pour l'instant.
 
Et donc, pour diminuer le volume on peut jouer sur l'une des deux variables : nombre de samplers ou pas (fréquence) dans le temps, le nombre de requêtes n'a pas d'impact cependant. 

On ne peut pas jouer sur le nombre de samplers différents car c'est ce qu'on veut tracer en terme de temps de réponse.
Et sur le pas, on pourrait accepter de ne pas pouvoir descendre en dessous de la seconde.
Je me suis pas trompé quelque part? 

Bonne journée à vous,

Jugurtha 

Le 18.03.2022 16:41, Philippe Mouawad a écrit :

Salut Guillaume,
Tu as bien compris.
 
C'est le nombre de samplers différents et le pas qui déterminent la quantité.
Mais je me rends compte que je pensais implicitement à un cas que je n'ai pas exprimé qui est celui ci et qui pose peut être problème.
 
En général on est intéressé par le "détail des erreurs" , cad pour un libellé d'erreur unique, on veut avoir le nombre d'erreur correspondantes
pour identifier à chaud pourquoi le tir est en train de mal se passer.
C'est cet élément qui peut parasiter la volumétrie si le type d'erreur est très variable parce qu'il y a un paramètre très variable dans une assertion, mais peut être qu'on peut mettre ça de côté pour l'instant

On Fri, Mar 18, 2022 at 4:33 PM Guillaume Copie <g.copie@ubik-ingenierie.com> wrote:
Salut Philippe,

il y a peut être quelque chose que j'ai mal compris dans le fonctionnement de JMeter et des Samplers et du coup j'ai du mal à voir où est le risque d'avoir un très / trop gros fichier. Voilà ce que j'ai compris

Un Sampler c'est par exemple une requête Http. Donc dans un tir de charge on peut vouloir tester différents endpoints et donc avoir autant de Sampler que d'endpoints

Pendant le tir, on veut pouvoir afficher les différentes informations pour chacun de ces Samplers pour un pas de temps donné.

Exemple, sur les 5 dernières secondes, il y a eu 1.000 requêtes http GET sur l'endpoint /toto, avec 0.1% d'erreur, un temps moyens de 50ms, un percentiles 1 à 15ms, percentiles 2 25ms, percentiles 3 100ms, temps max 300ms, débit 200 req/s

Donc qu'il y ait 200 req/s ou 1.000 req/s, il n'y a pas plus d'information à stocker

La volumétrie du fichier est uniquement liée aux nombres de Sampler configurés et au pas de temps donné

Donc si on imagine 25 Samplers, avec un pas de temps de 1s sur un tir de 2h, ça fait 25 x 7.200 lignes à sauvegarder donc 180.000 lignes. D'après le test fait par Jugurtha on serait autour de 20-25 Mo

Si on passe sur un pas de temps de 5s, on a plus que 36.000 lignes et donc un fichier autour de 5Mo

Est-ce que j'ai bien compris le fonctionnement ou j'ai oublié un multiplicateur quelque part ?

Si j'ai bien compris, est-ce qu'on peut imaginer jouer sur le pas de temps pour limiter la taille du fichier ? Exemple, si il y a beaucoup de Samplers, on augmente le pas de temps ?

Guillaume

Le 17/03/2022 à 17:30, Philippe Mouawad a écrit :
Salut Guillaume,
 
Pourquoi pas, vous pouvez regarder quel serait le volume du fichier pour un cas d'utilisation récent dont il faut multiplier le volume par 7 , le nombre de samplers différents serait de 25 et une durée de test de 2h :
https://www.ubik-ingenierie.com/blog/case-study-from-the-telecommunications-industry-performance-test-video-streaming-services-with-ubikloadpack/
Merci

On Thu, Mar 17, 2022 at 4:16 PM Guillaume Copie <g.copie@ubik-ingenierie.com> wrote:
Bonjour,

Est-ce qu'il y a vraiment besoin de la complexité d'une base de données ? En dehors d'un GET des infos sauvegardées il n'y aura pas d'opérations complexes

L'utilisation du localStorage était là pour que la partie sauvegarde de l'historique soit faite côté navigateur plutôt que JMeter. Si la sauvegarde se fait côté applicative, est-ce qu'une Map<String, Object> ne serait pas suffisante pour stocker les informations ?

Map qu'il serait possible de sauvegarder au format JSON dans un fichier texte pour une utilisation ultérieure ?

Guillaume

Le 17/03/2022 à 14:18, m.mons@ubik-ingenierie.com a écrit :
Bonjour,

Je suis d'accord. Je pense que c'est intéressant de garder ces données après le
test. Donc si on est trop limité avec un fichier, la base de données semble être
la bonne solution.

Cordialement,
Marie

Le 17.03.2022 13:10, Philippe Mouawad a écrit :

Bonjour Jugurtha,
Désolé pour la réponse tardive mais cette semaine est très chargée pour moi.
 
A la lecture (rapide cependant) de ton CR:
Je comprends qu'OpenMetrics est le format à privilégié, est-ce que Micro Meter le propose, à priori oui
https://github.com/micrometer-metrics/micrometer/pull/2486
Je pense qu'une base embarquée est préférable:
La limite LocalStorage est trop short, tu peux avoir plusieurs dizaine de sampler différents par seconde, donc on va arriver aux limites
Je me dis aussi que ça permettrait de récupérer les données après le test s'il y a moyen d'enregister la BDD
Par contre il faudrait une BDD Java Embedded avec la possibilité d'externaliser vers une base hors JVM
 
@Marie Mons , @Guillaume Copie , qu'en pensez-vous ?
Merci
Cordialement

On Tue, Mar 15, 2022 at 2:48 PM <j.kebir@ubik-ingenierie.com> wrote:
Bonjour Phillipe,

je me permets de te partager mon avancement dans les recherches d'un modèle de données qui convient et par rapport au localstorage (5MO).

En résumé:

    - Le modèle de données à Prometheus est consommable par divers systèmes de monitoring (Datadog, Instana et autres)

    - OpenMetrics viendra standardiser le modèle de Prometheus donc les formats sont censément identiques à l'exception de quelques améliorations (plus de data type..., tout est dans le document ci-joint)

    - Prometheus privilégie OpenMetrics avant autre chose

    - La taille du localstorage (5MO) peut-être suffisante si on respecte un nombre de tir maximal par seconde (plus de détails dans le rapport)

    - L'utilisation d'une base de données chargée en mémoire est envisageable aussi (HSQLDB)

Excellente journée,

Jugurtha

Le 14.03.2022 13:51, j.kebir@ubik-ingenierie.com a écrit :

Bonjour Philippe,

j'ai avancé dans mes recherches on peut faire une réunion cet après-midi si ça te va ou demain, j'ai fait un test sur un fichier de 5mo pour voir combien de caractères et lignes on peut stocker, ceci pour avoir une idée combien un tir peut coûter en stockage.

Bien à toi,

Jugurtha

Le 09.03.2022 22:40, Philippe Mouawad a écrit :

Bonjour Jughurta,
Mes réponses ci-dessous.

Cordialement

On Wed, Mar 9, 2022 at 5:52 PM <j.kebir@ubik-ingenierie.com> wrote:
Rebonjour Phillipe,

je voulais poser une question par rapport à l'historisation, pour voir si c'est une DB qui veut utiliser ou un local storage, je me suis dis que c'est important de savoir pour combien de temps on souhaite garder des historiques? (pour une session de navigateur? pour un nombre d'heures défini?), sachant que le local storage du navigateur est limité à 5MO, pour un realtime un localstrorage est mieux, si c'est pour une longue durée on peut partir sur une base de données, à savoir influxDB est adapté à ce genre de besoin d'historisation. 

Normalement c'est juste pendant la durée du tir, une fois le tir terminé, jmeter s'arrête et on n'a plus accès aux informations.
InfluxDB implique un tiers ce dont je ne veux pas.
Est-il envisageable d'embarquer une BDD:
https://www.baeldung.com/java-in-memory-databases


Le 09.03.2022 14:28, j.kebir@ubik-ingenierie.com a écrit :

Rebonjour Philippe,

ci-joint le compte rendu de notre réunion du 8/03/22.

Je vous prie, de me faire un retour c'est important pour moi pour savoir si je suis sur la bonne voie et si il y a des points à améliorer que ça soit dans ma manière d'aborder les choses et de les communiquer.

Je reste à disposition,

Jugurtha

Le 09.03.2022 13:34, j.kebir@ubik-ingenierie.com a écrit :

Bonjour Philippe,

je t'envoie ça sous quelques minutes (j'ai vu le mail que maintenant je regarderai ma boite mail plus souvent), j'avais fait un diagramme d'architecture hier je mets tout ça ensemble et je te transmets ça.

Jugurtha

Le 09.03.2022 08:00, Philippe Mouawad a écrit :

Bonjour Jughurta,
Peux-tu faire un compte-rendu de notre réunion d'hier ?
 
Merci

Cordialement
Philippe M.
Ubik-Ingenierie
# Page de doc 1 -> https://www.ubik-ingenierie.com/blog/monitor-jmeter-test-from-browser/
![Legende](images/doc1.png)
La remarque 3 n'est pas bien illustrée.
Il faudrait une image ou du texte pour dire que ça vient de add -> listener -> ULP observability


# Page de doc 2 -> https://www.ubik-ingenierie.com/blog/ubik-load-pack-observability-plugin/
![Legende](images/doc2-1.png)
je n'ai pas l'icone qui est entouré, j'ai du la trouver dans Options -> plugins managers. ça peut être bien de le mentionner si les gens n'ont pas non plus l'icone.

![Legende](images/doc2-2.png)
Jetty Metrics route -> je comprend pas bien ce qui est affiché sur cette route et à quoi elle sert.
Je suppose que c'est un truc utile pour le dev du plugin ?

Number of Processing Threads -> En quoi est-ce que c'est utile de faire varier le nombre de threads qui lisent les réponses aux requêtes ? Je suppose que àa optimise les calculs sur les réponses mais c'est pas explicite vu qu'on peut aussi augmenter le nombre de threads ailleurs dans Jmeter et que ça sert à simuler plus de traffique, ce qui n'est pas la même chose.

Log Frequency in seconds -> Il faut expliquer plus en détail certaines choses la dessus. J'ai vu qu'en mettant 60 (la valeur par défaut) dessus, si mon test dure 100 secondes, je n'ai aucun résultats qui s'affiche dans les graphs. Si je passe la durée du test à 500 secondes, j'ai des infos dans les graphiques. En lisant la doc on le finit par le comprendre, mais ce serait peut être plus clair en disant quelque chose du style "Si par exemple votre test dure + de 120 secondes, vous pouvez utiliser un 'Log Frequency in seconds' de 60 maximums, sans quoi vous n'aurez pas d'affichage dans les graphiques".

Sample Queue Buffer Capacity  -> Je vois pas bien ce que c'est. Mais la c'est peut être parceque j'ai pas fait de Jmeter depuis un moment ? En tout cas la valeur par défaut c'est 5000, mais c'est 5000 quoi ? La doc dit que ça contient des SamplerResults, mais je me représente mal ce que c'est ici. Pour moi un Sampler result je dirais que c'est ce qu'on peut voir dans un View Result Tree en mode GUI par exemple, sinon je vois pas trop. Du coup question -> est-ce que la doc doit être utilisable par un débutant en JMeter ou alors on suppose qu'ils ont forcément déjà des bonnes connaissances sur l'outil ?

# Des erreurs
J'ai essayé de le faire tourner en mode GUI pour commencer et voir ce que ça donne. J'ai rapidement fait un test plan très simple ou je le fais tourner 500 secondes, 5 threads. A chaque tour les threads faisaient un unique appel GET sur un localhost:3000 très basique quej 'ai fait rapidement. Ce endpoint renvoie une erreur 400 20% du temps, et met en moyenne 40 ms à répondre.

Je suppose que ce que j'ai fait ressemble au cas le plus basique possible, mais j'ai quand même des alertes d'erreurs sur la page avec les graphiques. En bas de l'image on peut voir le messagge d'erreur.
![Legende](images/errors-1.png)
-> Je viens de me rendre compte que j'avais bien les courbes bleues, elles étaient juste exactement superposées aux courbes rouges à chaques fois.

# Usage
Est-ce que les gens ne l'utilisent pas simplement car ils n'ont pas le besoin d'avoir les informations du test en direct ?
J'ai jamais fait de test JMeter sérieux donc je sais plus comment on fait pour avoir les résultats d'un gros test, mais est-ce que les gens ne se sont pas simplement habitués à lire ces résultats la et n'ont donc pas spécialement besoin d'utiliser ce plugin ?
De mémoire les résultats qu'ont obitent avec des tests JMeter c'est aussi des graphs sur de l'html/css si je dis pas de bêtises -> c'est assez similaire à ce plugin non ?

# Installation
Je n'ai eu aucun problème pour l'installation, tout a marché du premier coup. On peut mentionner le fait qu'il faut pas mal scroller pour trouver le plugin dans la liste des plugin une fois qu'on a ajouté le jar. J'en avais un bon paquet contrairement à l'image sur la doc ou en voit que 4.

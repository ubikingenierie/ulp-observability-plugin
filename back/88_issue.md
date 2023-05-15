# Le besoin
1. Définir un nombre des X premières erreurs depuis l'interface utilisteur.
2. Enregsitrer les X premiers erreurs sans dépasser la mémoire.
3. Avoir une méthode d'API pour exposer ce service.

# Comment faire ?
Pour un sampler contenant une erreur, on ajoute dans le registre "summary" un compteur pour ce type d'erreur associé au sampler.

Ensuite on peut déterminer le nombre total d'erreurs par type. Comment faire ? pour chaque type d'erreur, on additionne les compteurs pour tous les samplers.

Cette solution permettra aussi de filtrer les types d'erreurs par sampler.
## Le code à changer
### Pré-condition
Il faut extraire le code d'erreur d'un SampleResult si celui-ci contient une erreur. Cela doit se faire dans ``ULPObservabilityListener>>sampleOccurred()``, pendant la création du ResponseResult.
### Implémentation
Dans le code de la méthode MicrometerRegistry>>addResponse(ResponseResult result) on doit ajouter un tag supplémentaire (type d'erreur) lorsque le sampler contient une erreur. 
Pour obtenir le nombre total d'erreurs pour un type d'erreur donné, on va définir une méthode ``Long getTotalErrorsForType(String errorType)`` qui trouvera les compteurs correspondant à ce type d'erreur à partir du ``summaryRegistry``, puis sommer les compteurs pour obtenir le total.

Enfin, il faut regrouper les types d'erreur et leur nombre d'occurence dans une Map. Cette Map sera construite dans la méthode ``SampleLog makeLog(String errorType)`` du ``MicrometerRegistry`` pour qu'elle soit fournit comme paramètre à l'objet SampleLog().




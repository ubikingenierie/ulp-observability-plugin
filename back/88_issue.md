# Le besoin
1. Définir un nombre des X premières erreurs depuis l'interface utilisteur.
2. Enregsitrer les X premiers erreurs sans dépasser la mémoire.
3. Avoir une méthode d'API pour exposer ce service.

# Comment faire ?
Pour un sampler contenant une erreur, on ajoute dans le registre "summary" un compteur pour ce type d'erreur associé au sampler.

Ensuite on peut déterminer le nombre total d'erreurs par type. Comment faire ? pour chaque type d'erreur, on additionne les compteurs pour tous les samplers.
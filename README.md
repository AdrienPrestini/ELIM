#Projet ELIM 2017-2018
========================

##Encadrant : 
------------

	* Jean-Yves TIGLI : Jean-Yves.TIGLI@unice.fr

##Étudiants : 
-------------

	* Nicolas PÉPIN : nicolas.pepin.06@gmail.com
	* Adrien PRESTINI : adrien.prestini@etu.unice.fr
	
	
##Présentation du projet
------------------------

Ce projet permet de mettre en avant la programmation avancée en développement mobile.
Notre projet est de développer une application qui permet la détection de magasins sur notre trajet à proximité de notre position, et nous affiche les magasins qui nous interesse.

Ce projet sera sous la plateforme Android exclusivement (avec l'IDE Android Studio) et sera testé sur nos smartphones personnels (utilisation de la position GPS).

Le projet se réalise sous forme de sprint (voir le fichier Gantt).
A noter que le Gantt prévionnel n'est donné qu'à titre indicatif et ne présente que les directives à suivre pour l'accomplissement du projet.

##Serveur
------------------------

Le serveur a été développé avec node.js.
Le serveur utilise bayes-classifier, ce module permet de réaliser catégories de mot (ex: "buger" correpond à la catégorie "restaurant").
Afin de récupérer les magasins correpondant à la recherche de l'utilisateur et par rapport à sa position le serveur utilisde google-places de Google. 
Ce module prend en entrée les paramètres tel que le mot clé et la position de l'utilisateur et le module retourne un json ave les magasins correspondants
à la recherche effectuée.
Chaque recherche effectuées est enregistré dans une base de donnée mySql afin d'offir la prédiction des recherches en fonctions des recherches faites précédement.
Pour lancer le serveur il suffit de lancer le index.js avec:
node index.js

On peut le lancer aussi avec docker, pour cela il faut que docker soit installé sur votre serveur ou machine puis, que mysql de docker contienne la data base et 
la table "recherche", voir le fichier elim.sql qui est un exemple de la db. Il va falloir lier les deux containers (mySql et elim)
https://hub.docker.com/r/mysql/mysql-server/
https://buddy.works/guides/how-dockerize-node-application

Pour lancer, effectuer:

docker build -t elim .

et enfin:

docker run -p 8080:8080 elim

##Base de données
------------------------

La base de donnée est simple, elle est constitué d'une table "recherche" avec:
- idRecherche qui est un auto increment
- imei qui correspond à l'utilisateur qui a effectué la recherche
- motCherche qui correspond au mot recherché
- dateRecherche sous le format "AAAA-MM-JJ"
- heureRecherche sous le format "HH-MM-SS"
- jourSemaine un numéro avec 0 pour Dimanche, 1 pour Lundi ... 6 pour Samedi

##Scénario type
------------------------

1) Le serveur et la base de données sont actifs. 
2) L'utilisateur est sur l'application et réalise une recherche du mot "la Casa"(restaurant). 
3) L'application réalise une requette post auprès du serveur avec l'Imei du téléphone, le mot cherché et la position de l'utilisateur.
4) Recherche auprès de l'API Google les commerces qui correspondent à la recherche. 
5) Retour des informations de l'API à l'utilisateur. 
6) Si le résultat n'est pas vide, on recherche dans le bayes-classifier si le mot cherché est déjà enregistré et s'il correspond à une catégorie. 
7) S'il ne l'est pas, ajout d'une nouevelle catégoerie au bayes-classifier. 
8) On insère la recherche de l'utilsiateur avec les inforamtions recu et la catégorie auquel appartient le mot. 



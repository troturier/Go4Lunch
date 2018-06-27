# Go4Lunch
## Trouvez un restaurant pour déjeuner avec vos collègues

[![Build Status](https://travis-ci.org/troturier/Go4Lunch.svg?branch=master)](https://travis-ci.org/troturier/Go4Lunch) [![Coverage Status](https://coveralls.io/repos/github/troturier/Go4Lunch/badge.svg?branch=master)](https://coveralls.io/github/troturier/Go4Lunch?branch=master) 

### Introduction
L’application Go4Lunch est une application collaborative utilisée par tous les employés. Elle permet de rechercher un restaurant dans les environs, puis de sélectionner celui de son choix en en faisant part à ses collègues. De la même manière, il est possible de consulter les restaurants sélectionnés par les collègues afin de se joindre à eux. Un peu avant l’heure du déjeuner, l’application notifie les différents employés pour les inviter à rejoindre leurs collègues.

---

### Back-end
Pour fonctionner correctement, l’application mobile a besoin de dialoguer avec un serveur, plus communément appelé [back-end](https://en.wikipedia.org/wiki/Front_and_back_ends). Afin de simplifier l’implémentation, Go4Lunch se repose sur le back-end [Firebase](https://firebase.google.com/) proposé par Google. Ce service permet de gérer très facilement :

- Les comptes utilisateur ;
- L’authentification des utilisateurs via des services tiers (dont Facebook et Google, bien évidemment) ;
- La sauvegarde des données ;
- L’envoi de messages Push.

---

### Connexion
L’accès à l’application est restreint : il est impératif de se connecter avec un compte Google ou Facebook.

![alt text](https://user.oc-static.com/upload/2017/05/23/14955526646853_Login%20Screen.png "Connexion")

---

### Écran d'accueil
L’application est composée de trois vues principales, accessibles grâce à trois boutons situés en bas de l’écran :

- La vue des restaurants sous forme de carte ;
- La vue des restaurants sous forme de liste ;
- La vue des collègues qui utilisent l’application.

Une fois l’utilisateur connecté, l’application affiche par défaut la vue des restaurants sous forme de carte.

---

### Vue des restaurants sous forme de carte
L’utilisateur est automatiquement géo-localisé par l’application, afin d’afficher le quartier dans lequel il se trouve. Tous les restaurants des alentours sont affichés sur la carte en utilisant une punaise personnalisée. Si au moins un collègue s’est déjà manifesté pour aller dans un restaurant donné, la punaise est affichée dans une couleur différente (verte). L’utilisateur peut appuyer sur une punaise pour afficher la fiche du restaurant, décrite plus bas. Un bouton de géolocalisation permet de recentrer automatiquement la carte sur l’utilisateur.

![alt text](https://user.oc-static.com/upload/2017/05/23/14955527972631_Map%20Screen.png "Carte")

---

### Vue des restaurants sous forme de liste
Cette vue permet d’afficher le détail des restaurants qui se situent sur la carte. Pour chaque restaurant, les informations suivantes sont affichées :

- Le nom du restaurant ;
- La distance du restaurant par rapport à l’utilisateur ;
- Une image du restaurant (si disponible) ;
- Le type de restaurant [optionnel car non supporté par l'API Google Places] ;
- L’adresse du restaurant ;
- Le nombre de collègues qui se sont déclarés intéressés à y aller ;
- Les horaires d’ouverture du restaurant ;
- Le nombre d’avis favorables sur ce restaurant (entre 0 et 3 étoiles).

![alt text](https://user.oc-static.com/upload/2017/05/23/14955530288801_List%20Screen.png "Liste")

---

### Fiche détaillée d'un restaurant
Lorsque l’utilisateur clique sur un restaurant (depuis la carte ou depuis la liste), un nouvel écran apparaît pour afficher le détail de ce restaurant. Les informations affichées reprennent celles de la liste, et également :

- Un bouton permettant d'indiquer son choix de restaurant ;
- Un bouton permettant d’appeler le restaurant (sous réserve qu’un numéro soit disponible) ;
- Un bouton permettant d’aimer le restaurant (Like) que vous stockerez sur Firebase ;
- Un bouton permettant d’accéder au site du restaurant (sous réserve que le restaurant possède un site) ;
- La liste des collègues déclarés intéressés à aller déjeuner dans ce restaurant. Si aucun collègue ne s’est manifesté, aucune liste n’est affichée.

![alt text](https://user.oc-static.com/upload/2017/05/23/14955545199687_Restaurant%20Detail%20Screen.png "Détail")

---

### Liste des collègues
Cet écran affiche la liste de tous vos collègues, avec leur choix de restaurant. Si un collègue a choisi un restaurant, vous pouvez appuyer dessus (sur l'écran, pas sur votre collègue) pour afficher la fiche détaillée de ce restaurant.

![alt text](https://user.oc-static.com/upload/2017/05/23/14955546022557_Co-Workers%20Screen.png "Liste collègues")

---

### Fonctionnalité de recherche
Sur chaque vue, une loupe située en haut à droite de l’écran permet d’afficher une zone de recherche. Cette recherche est contextuelle, et met automatiquement à jour les données de la vue correspondante. Par exemple, si vous tapez “japonais” sur la vue cartographique, seuls les restaurants japonais s’affichent sur la carte. Si aucun restaurant japonais n’est présent dans les alentours, tant-pis pour vous !

![alt text](https://user.oc-static.com/upload/2017/05/24/14956345469069_Search%20Screens.png "Recherche")

---

### Menu
En haut à gauche se situe un bouton de menu. Non, ce n’est pas le menu du déjeuner. En cliquant dessus, un menu latéral s’affiche, avec les informations suivantes :

- Votre photo de profil ;
- Votre prénom et votre nom, au cas où vous ne sachiez plus comment vous vous appelez ;
- Un bouton permettant d’afficher le restaurant où vous avez prévu d’aller déjeuner ;
- Un bouton permettant d’accéder à l’écran des paramètres (pour configurer par exemple la gestion des notifications) ;
- Un bouton permettant de vous déconnecter et de retourner à l’écran de connexion.

![alt text](https://user.oc-static.com/upload/2017/05/23/14955550617381_Menu%20Screen.png "Menu")

---

### Notifications
Un message de notification devra être automatiquement envoyé à tous les utilisateurs qui ont sélectionné un restaurant dans l'application. Le message sera envoyé à 12h. Il rappellera à l'utilisateur le nom du restaurant qu'il a choisi, l'adresse, ainsi que la liste des collègues qui viendront avec lui.

---

### Traduction
Vos collègues étant de toutes les nationalités, vous devrez a minima proposer une version française et anglaise de l’application.



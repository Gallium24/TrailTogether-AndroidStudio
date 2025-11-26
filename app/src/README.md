# TrailTogether v0.1 - L'application

TrailTogether est une application mobile Android conçue pour les amateurs de randonnée. 
Elle doit permettre aux utilisateurs de découvrir des sentiers, de partager leurs expériences via un fil d'actualité et de planifier leurs sorties. 

L'application est développée en Kotlin avec Jetpack Compose pour l'interface utilisateur et utilise Firebase pour l'authentification et la base de données en temps réel.

## TrailTogether v0.1 - Architecture Générale

L'application suit une architecture MVVM (Model-View-ViewModel), séparant clairement les responsabilités :

-   **View (UI)** : Les écrans, construits avec Jetpack Compose (`/ui/screens`). Ils sont responsables de l'affichage des données et de la capture des interactions utilisateur.
-   **ViewModel** : Les classes (`/data/viewmodel`) qui préparent et gèrent les données pour l'UI. Elles survivent aux changements de configuration et communiquent avec la couche de données.
-   **Model (Data)** : La couche de données, composée du **Repository** qui centralise l'accès aux données, et des **modèles** de données (`/data/models`) qui définissent la structure des objets (Utilisateur, Post, Sentier, Événement, Notification, Commentaire).

### TrailTogether v0.1 - Flux de Données

1.  **Firebase** : Contient les collections d'utilisateurs, de randonnées (`trails`), de posts, etc.
2.  **Repository (`FirestoreRepository.kt`)** : C'est le seul point de contact avec Firebase. Il expose les données sous forme de `Flow` (flux de données réactifs).
3.  **Repository (`CompletTrailRepository.kt`)** : Point de contact avec l'API Overpass pour les sentiers OSM.
4.  **Repository (`GeoCodingService.kt`)** : Service de géocodage inversé pour convertir coordonnées GPS en adresses.
3.  **ViewModel** : S'abonne aux `Flow` du Repository, applique la logique métier (filtres, recherches) et expose l'état (`StateFlow`) à l'UI.
4.  **UI (Composable)** : Observe les `StateFlow` du ViewModel et se recompose automatiquement lorsque les données changent. Les actions de l'utilisateur (clics) appellent des fonctions sur le ViewModel.
5.  **OpenStreetMap (API Overpass)** : Source de données pour les sentiers de randonnée en temps réel.

---

## TrailTogether v0.1 - Détail des Fichiers et Dossiers

### `app/src/main/java/com/example/trailtogether_v01/`

#### `data/` : Couche de données

-   **`models/`**
      -   `Comment.kt` : Définit la structure d'un commentaire sur un post.
      -   `Event.kt`: Définit la structure d'une sortie.
      -   `Notification.kt`: Définit la structure d'une notification in-app (likes, commentaires).
      -   `Post.kt`: Définit la structure d'une publication dans le fil d'actualité.
      -   `Trail.kt`: Définit la structure d'un sentier de randonnée (nom, difficulté, coordonnées, etc.).
      -   `User.kt`: Définit la structure des données d'un utilisateur (nom, bio, contact d'urgence).
-   **`repository/`**
    -   `FirestoreRepository.kt`: **Le Cœur de la couche de données**. Centralise toutes les requêtes vers la base de données Firestore (récupérer les sentiers, les posts, mettre à jour un profil, gérer les likes). Il cache la complexité de Firebase aux ViewModels.
    -   `GeoCodingService.kt`: Service de géocodage inversé pour convertir coordonnées GPS en adresses.
    -   `CompletTrailRepository.kt`: Point de contact avec l'API Overpass pour les chemins de randonnée en temps réel.

-   **`viewmodel/`**
  -   `AuthViewModel.kt`: Gère la logique d'authentification : inscription, connexion (email/mot de passe et Google), et déconnexion.
  -   `CalendarViewModel.kt`: Gère l'état du calendrier (`CalendarScreen`).
  -   `CommentViewModel.kt`:  Gère l'affichage et l'ajout de commentaires sur les posts.
  -   `EventDetailViewModel.kt`: Gère les détails d'un événement (participants, inscription/désinscription).
  -   `FeedViewModel.kt`: Gère l'état du fil d'actualité (`FeedScreen`) et la création de posts (`CreatePostScreen`).
  -   `HistoryViewModel.kt`: Gère l'historique des randonnées de l'utilisateur.
  -   `HomeViewModel.kt`: Gère l'état de l'écran d'accueil (`HomeScreen`), notamment la recherche et le filtrage des sentiers.
  -   `NotificationViewModel.kt`: Gère l'état des notifications (`NotificationScreen`).
  -   `ProfileViewModel.kt`: Gère l'état de l'écran de profil (`ProfileScreen`) et la mise à jour des informations utilisateur.
  -   `SettingsViewModel.kt`: Gère les paramètres de l'application (mode sombre, unités, rayon de recherche, style de carte).
  -   `TrailDetailViewModel.kt`: Gère l'état des détails d'un itinéraire (`TrailDetailScreen`).
  -   `UserProfileViewModel.kt`: Gère l'affichage du profil d'un autre utilisateur.

---

#### `navigation/` : Gestion de la navigation

-   `AuthNavGraph.kt`: Définit le graphe de navigation pour la partie **authentification** de l'application (les écrans `LoginScreen` et `RegisterScreen`).
-   `MainNavGraph.kt`: Définit le graphe de navigation interne de l'application
-   `RootNavGraph.kt`: Définit le graphe de navigation racine de l'application. Il détermine quel graphe de navigation afficher en fonction de l'état d'authentification de l'utilisateur. Gère aussi l'initialisation de la position GPS.
-   `Screen.kt`: Contient un `sealed class` qui définit toutes les routes (chemins de navigation) de l'application de manière centralisée et sécurisée.

#### `services/` : Services système

-   `LocationService.kt`:  Gère la récupération de la position GPS de l'utilisateur au démarrage. Gère les permissions de localisation et le fallback vers une position par défaut.

#### `ui/` : Couche de présentation (UI)

-   **`components/`**: Contient les éléments d'UI réutilisables à travers l'application.
    -   `BottomNavBar.kt`: La barre de navigation inférieure principale.
    -   `EmergencyDialog`: La boite de dialogue pour les notifications d'urgence. 
    -   `EventCard.kt`:  Carte affichant les informations d'un événement (date, lieu, participants).
    -   `FilterChips.kt`: Les "chips" (boutons) pour filtrer les sentiers. 
    -   `TrailCard.kt`: La carte qui affiche les informations d'un sentier dans une liste.
    -   `PostCard.kt`: La carte qui affiche les informations d'une publication dans le fil d'actualité.
    - 
-   **`screens/`**: Contient les écrans complets de l'application.
   -   **`auth/`**: Écrans liés à l'authentification.
       -   `LoginScreen.kt`: Écran de connexion (email/mot de passe et Google).
       -   `RegisterScreen.kt`: Écran d'inscription.
   -   **`calendar/`**:
       -   `CalendarScreen.kt`: Écran du calendrier.
       -   `CreateEventScreen.kt`: Formulaire de création d'événement lié à un sentier.
       -   `EventDetailScreen.kt`: Affiche les détails d'une sortie.
   -   **`feed/`**:
       -   `FeedScreen.kt`: Affiche la liste des posts des utilisateurs.
       -   `CreatePostScreen.kt`: Formulaire pour créer un nouveau post.
       -   `CommentsScreen.kt` : Liste des commentaires d'un post avec ajout de commentaires.
   -   **`home/`**:
       -   `HomeScreen.kt`: Écran principal après connexion. Affiche la carte interactive et la liste filtrable des sentiers.
       -   `TrailDetailScreen.kt`: Affiche les détails complets d'un sentier sélectionné.
   -   **`notifications/`**:
       -   `NotificationsScreen.kt`: Liste des notifications avec distinction visuelles (lues/non lues) et bouton "Tout marquer comme lu".
   - **`profile/`**:
       -   `ProfileScreen.kt`: Affiche les informations du profil de l'utilisateur.
       -   `EditProfileScreen.kt`: Formulaire pour modifier les informations du profil.
       -   `HistoryScreen.kt`: Historique complet des randonnées effectuées.
       -   `UserProfileScreen.kt`: Profil d'un autre utilisateur (vue publique).
   -   **`settings/`**:
       -   `SettingsScreen.kt`: Écran des paramètres (mode sombre, unités de mesure, rayon de recherche, style de carte, contact d'urgence).
       
- **`theme/`**:
    -   `Theme.kt`: Définit le thème de l'application (couleurs, formes, typographie).
    -   `Color.kt`: Contient les couleurs utilisées dans l'application.
    -   `Type.kt`: Contient les styles de typographie.
  
-   **`utils/`**:
    -   `Constants.kt`: Constantes de l'application (clés SharedPreferences, valeurs par défaut).
    -   `DateUtils.kt`: Fonctions utilitaires pour le formatage des dates.
    -   `EmailService.kt`: Service d'envoi d'emails d'urgence.
    -   `FormatUtils.kt`: Fonctions de formatage (distances avec unités km/miles).
    -   `MapUtils.kt`: Utilitaires pour la carte (sources de tuiles personnalisées comme OpenTopoMap).
-   **`workers/`**:
    -   `EmergencyWorker.kt`: Worker pour l'envoi de SMS d'urgence en arrière-plan.
    
-   `MainActivity.kt`: Point d'entrée principal de l'application. Gère le splash screen, l'initialisation d'OSMDroid, la demande de permission de localisation et l'initialisation du graphe de navigation.

---

## Dépendances Clés

-   **Jetpack Compose**: Pour la construction de l'interface utilisateur déclarative.
-   **Navigation Compose**: Pour la gestion de la navigation entre les écrans.
-   **ViewModel Compose**: Pour l'intégration facile des ViewModels dans l'UI Compose.
-   **Firebase-BOM**: Pour gérer les versions des dépendances Firebase (Auth, Firestore).
-   **OSMDroid**: Pour l'affichage de cartes open-source (OpenStreetMap).

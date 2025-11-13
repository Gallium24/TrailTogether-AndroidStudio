# TrailTogether v0.1 - L'application

TrailTogether est une application mobile Android conçue pour les amateurs de randonnée. 
Elle doit permettre aux utilisateurs de découvrir des sentiers, de partager leurs expériences via un fil d'actualité et de planifier leurs sorties. 

L'application est développée en Kotlin avec Jetpack Compose pour l'interface utilisateur et utilise Firebase pour l'authentification et la base de données en temps réel.

## TrailTogether v0.1 - Architecture Générale

L'application suit une architecture MVVM (Model-View-ViewModel), séparant clairement les responsabilités :

-   **View (UI)** : Les écrans, construits avec Jetpack Compose (`/ui/screens`). Ils sont responsables de l'affichage des données et de la capture des interactions utilisateur.
-   **ViewModel** : Les classes (`/data/viewmodel`) qui préparent et gèrent les données pour l'UI. Elles survivent aux changements de configuration et communiquent avec la couche de données.
-   **Model (Data)** : La couche de données, composée du **Repository** qui centralise l'accès aux données, et des **modèles** de données (`/data/models`) qui définissent la structure des objets (Utilisateur, Post, Randonnée).

### TrailTogether v0.1 - Flux de Données

1.  **Firebase** : Contient les collections d'utilisateurs, de randonnées (`trails`), de posts, etc.
2.  **Repository (`FirestoreRepository.kt`)** : C'est le seul point de contact avec Firebase. Il expose les données sous forme de `Flow` (flux de données réactifs).
3.  **ViewModel** : S'abonne aux `Flow` du Repository, applique la logique métier (filtres, recherches) et expose l'état (`StateFlow`) à l'UI.
4.  **UI (Composable)** : Observe les `StateFlow` du ViewModel et se recompose automatiquement lorsque les données changent. Les actions de l'utilisateur (clics) appellent des fonctions sur le ViewModel.

---

## TrailTogether v0.1 - Détail des Fichiers et Dossiers

### `app/src/main/java/com/example/trailtogether_v01/`

#### `data/` : Couche de données

-   **`models/`**
      -   `Event.kt`: Définit la structure d'une sortie.
      -   `Post.kt`: Définit la structure d'une publication dans le fil d'actualité.
      -   `Trail.kt`: Définit la structure d'un sentier de randonnée (nom, difficulté, coordonnées, etc.).
      -   `User.kt`: Définit la structure des données d'un utilisateur (nom, bio, contact d'urgence).
-   **`repository/`**
    -   `FirestoreRepository.kt`: **Le Cœur de la couche de données**. Centralise toutes les requêtes vers la base de données Firestore (récupérer les sentiers, les posts, mettre à jour un profil, gérer les likes). Il cache la complexité de Firebase aux ViewModels.
-   **`viewmodel/`**
  -   `AuthState.kt`: Définit l'état d'authentification de l'utilisateur pour être utilisé dans AuthViewModel.kt.
  -   `AuthViewModel.kt`: Gère la logique d'authentification : inscription, connexion (email/mot de passe et Google), et déconnexion.
  -   `FeedViewModel.kt`: Gère l'état du fil d'actualité (`FeedScreen`) et la création de posts (`CreatePostScreen`).
  -   `HomeViewModel.kt`: Gère l'état de l'écran d'accueil (`HomeScreen`), notamment la recherche et le filtrage des sentiers.
  -   `ProfileViewModel.kt`: Gère l'état de l'écran de profil (`ProfileScreen`) et la mise à jour des informations utilisateur.
  -   `TrailDetailViewModel.kt`: Gère l'état des détails d'un itinéraire (`TrailDetailScreen`).

#### `navigation/` : Gestion de la navigation

-   `AuthNavGraph.kt`: Définit le graphe de navigation pour la partie **authentification** de l'application (les écrans `LoginScreen` et `RegisterScreen`).
-   `MainNavGraph.kt`: Définit le graphe de navigation interne de l'application
-   `RootNavGraph.kt`: Définit le graphe de navigation racine de l'application. Il détermine quel graphe de navigation afficher en fonction de l'état d'authentification de l'utilisateur.
-   `Screen.kt`: Contient un `sealed class` qui définit toutes les routes (chemins de navigation) de l'application de manière centralisée et sécurisée.

#### `ui/` : Couche de présentation (UI)

-   **`components/`**: Contient les éléments d'UI réutilisables à travers l'application.
    -   `BottomNavBar.kt`: La barre de navigation inférieure principale.
    -   `EmergencyDialog`: La boite de dialogue pour les notifications d'urgence. (à développer)
    -   `EventCard.kt`: La carte qui affiche les informations d'une sortie. (à développer)
    -   `FilterChips.kt`: Les "chips" (boutons) pour filtrer les sentiers. (à développer)
    -   `TrailCard.kt`: La carte qui affiche les informations d'un sentier dans une liste.
    -   `PostCard.kt`: La carte qui affiche les informations d'une publication dans le fil d'actualité.
    - 
-   **`screens/`**: Contient les écrans complets de l'application.
   -   **`auth/`**: Écrans liés à l'authentification.
       -   `LoginScreen.kt`: Écran de connexion (email/mot de passe et Google).
       -   `RegisterScreen.kt`: Écran d'inscription.
   -   **`calendar/`**:
       -   `CalendarScreen.kt`: Écran du calendrier (à développer).
       -   `EventDetailScreen.kt`: Affiche les détails d'une sortie (à développer).
   -   **`feed/`**:
       -   `FeedScreen.kt`: Affiche la liste des posts des utilisateurs.
       -   `CreatePostScreen.kt`: Formulaire pour créer un nouveau post.
   -   **`home/`**:
       -   `HomeScreen.kt`: Écran principal après connexion. Affiche la carte interactive et la liste filtrable des sentiers.
       -   `TrailDetailScreen.kt`: Affiche les détails complets d'un sentier sélectionné.
   - **`profile/`**:
       -   `ProfileScreen.kt`: Affiche les informations du profil de l'utilisateur.
       -   `EditProfileScreen.kt`: Formulaire pour modifier les informations du profil.
   - 
-   **`theme/`**:
    -   `Theme.kt`: Définit le thème de l'application (couleurs, formes, typographie).
    -   `Color.kt`: Contient les couleurs utilisées dans l'application.
    -   `Type.kt`: Contient les styles de typographie.

-   `MainActivity.kt`: Point d'entrée principal de l'application.

---

## Dépendances Clés

-   **Jetpack Compose**: Pour la construction de l'interface utilisateur déclarative.
-   **Navigation Compose**: Pour la gestion de la navigation entre les écrans.
-   **ViewModel Compose**: Pour l'intégration facile des ViewModels dans l'UI Compose.
-   **Firebase-BOM**: Pour gérer les versions des dépendances Firebase (Auth, Firestore).
-   **OSMDroid**: Pour l'affichage de cartes open-source (OpenStreetMap).

package com.example.trailtogether_v01.data.repository

import android.util.Log
import com.example.trailtogether_v01.data.models.*
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

/**
 * FirestoreRepository est la seule source de vérité pour l'accès aux données distantes sur Firestore.
 * Il agit comme un médiateur entre les ViewModels et la base de données Firebase.
 * Il expose les données sous forme de Flow pour permettre une observation réactive des changements.
 *
 * Responsabilités :
 * - Récupérer la liste des sentiers (trails) et des posts.
 * - Gérer les opérations sur les posts (création, like).
 * - Gérer les opérations sur les utilisateurs (récupération, mise à jour du profil).
 *
 * Cette classe abstrait complètement la complexité de Firestore pour le reste de l'application.
 */
class FirestoreRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    // Note: Les méthodes login/register ne sont pas utilisées dans AuthViewModel car l'auth est gérée directement là-bas.
    // Mais si besoin, on peut les implémenter avec Firestore pour stocker l'utilisateur après création.

    fun getCurrentUser(): Flow<User?> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject<User>(User::class.java)
                trySend(user)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateUser(user: User) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).set(user).await()
    }

    // Trails
    fun getTrails(): Flow<List<Trail>> = callbackFlow {
        val listener = firestore.collection("trails")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val trails = snapshot?.documents?.mapNotNull { it.toObject<Trail>(Trail::class.java) } ?: emptyList()
                trySend(trails)
            }
        awaitClose { listener.remove() }
    }

    fun getTrailById(id: String): Flow<Trail?> = callbackFlow {
        // Si c'est un trail OSM importé, on cherche dans le cache, sinon dans la collection principale
        val collectionName = if (id.startsWith("osm_")) "cached_trails" else "trails"

        val listener = firestore.collection(collectionName).document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val trail = snapshot?.toObject<Trail>(Trail::class.java)
                trySend(trail)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveTrail(trail: Trail) {
        try {
            // On sauvegarde dans 'cached_trails' pour ne pas polluer la recherche principale
            firestore.collection("cached_trails").document(trail.id)
                .set(trail, com.google.firebase.firestore.SetOptions.merge())
                .await()
            Log.d("FirestoreRepository", "Trail sauvegardé dans l'historique cache: ${trail.name}")
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Erreur sauvegarde trail", e)
        }
    }

    // Events
    fun getEvents(): Flow<List<Event>> = callbackFlow {
        val listener = firestore.collection("events")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val events = snapshot?.documents?.mapNotNull { it.toObject<Event>(Event::class.java) } ?: emptyList()
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    fun getEventsByDate(date: String): Flow<List<Event>> = callbackFlow {
        val listener = firestore.collection("events")
            .whereEqualTo("date", date)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val events = snapshot?.documents?.mapNotNull { it.toObject<Event>(Event::class.java) } ?: emptyList()
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    // Posts
    fun getPosts(): Flow<List<Post>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid ?: run {
            trySend(emptyList()) // Envoyer une liste vide si pas d'utilisateur
            close()
            return@callbackFlow
        }
        val listener = firestore.collection("posts")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                val posts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject<Post>()?.let { post ->
                        post.copy(
                            id = doc.id,
                            isLiked = (doc["likedBy"] as? List<*>)?.contains(currentUserId) ?: false
                        )
                    }
                }
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createPost(trailId: String, content: String): Result<Post> {
        val currentUser = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
        val post = Post(
            id = UUID.randomUUID().toString(),
            authorId = currentUser.uid,
            authorName = currentUser.displayName ?: "Anonymous",
            authorUsername = currentUser.displayName ?: "anonymous", // À ajuster si username est stocké ailleurs
            trailId = trailId,
            trailName = "", // À fetch si besoin, ou passer en paramètre
            content = content,
            likesCount = 0,
            commentsCount = 0,
            timestamp = Timestamp.now()
        )
        firestore.collection("posts").document(post.id).set(post).await()
        return Result.success(post)
    }

    suspend fun toggleLikePost(postId: String, isCurrentlyLiked: Boolean) {
        val currentUserId = auth.currentUser?.uid ?: return

        // On s'assure que postId n'est pas vide pour éviter le crash.
        if (postId.isBlank()) {
            Log.e("FirestoreRepository", "Tentative de liker un post avec un ID vide.")
            return
        }

        // On construit la référence au document
        val postRef = db.collection("posts").document(postId)

        val updateAction = if (isCurrentlyLiked) {
            FieldValue.arrayRemove(currentUserId)
        } else {
            FieldValue.arrayUnion(currentUserId)
        }

        val likesCountUpdate = if(isCurrentlyLiked) {
            FieldValue.increment(-1)
        } else {
            FieldValue.increment(1)
        }

        try {
            db.runTransaction { transaction ->
                transaction.update(postRef, "likedBy", updateAction)
                transaction.update(postRef, "likesCount", likesCountUpdate)
            }.await()
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Erreur lors de la mise à jour du like", e)
        }
    }

    //Notif
    fun getNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val listener = firestore.collection("notifications")
            .whereEqualTo("recipientId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val notifications = snapshot?.documents?.mapNotNull {
                    it.toObject<Notification>()?.copy(id = it.id)
                } ?: emptyList()
                trySend(notifications)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createNotification(notification: Notification) {
        try {
            val notificationId = firestore.collection("notifications").document().id
            val notificationWithId = notification.copy(id = notificationId)

            firestore.collection("notifications")
                .document(notificationId)
                .set(notificationWithId)
                .await()
            Log.d("FirestoreRepository", "Notification créée")
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Erreur création notification", e)
        }
    }

    suspend fun markNotificationAsRead(notificationId: String) {
        if (notificationId.isBlank()) {
            return
        }
        try {
            firestore.collection("notifications")
                .document(notificationId)
                .update("read", true)
                .await()
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Erreur marquage notification", e)
        }
    }

    suspend fun getUnreadNotificationCount(userId: String): Int {
        return try {
            val snapshot = firestore.collection("notifications")
                .whereEqualTo("recipientId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Erreur comptage notifications", e)
            0
        }
    }

    // Méthode helper pour récupérer l'auteur d'un post
    suspend fun getPostAuthorId(postId: String): String? {
        return try {
            val snapshot = firestore.collection("posts")
                .document(postId)
                .get()
                .await()
            snapshot.getString("authorId")
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Erreur récupération auteur", e)
            null
        }
    }



    // --- Gestion des Profils Publics & Abonnements ---

    fun getUserById(userId: String): Flow<User?> = callbackFlow {
        val listener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject<User>())
            }
        awaitClose { listener.remove() }
    }

    fun getPostsByAuthor(authorId: String): Flow<List<Post>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        val listener = firestore.collection("posts")
            .whereEqualTo("authorId", authorId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val posts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<Post>()?.let { post ->
                        post.copy(
                            id = doc.id,
                            isLiked = (doc["likedBy"] as? List<*>)?.contains(currentUserId) ?: false
                        )
                    }
                } ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    fun isFollowing(targetUserId: String): Flow<Boolean> = callbackFlow {
        val currentUserId = auth.currentUser?.uid ?: run {
            trySend(false)
            close()
            return@callbackFlow
        }

        // On vérifie l'existence d'un document dans la sous-collection "following"
        val listener = firestore.collection("users").document(currentUserId)
            .collection("following").document(targetUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(false)
                    return@addSnapshotListener
                }
                trySend(snapshot != null && snapshot.exists())
            }
        awaitClose { listener.remove() }
    }

    suspend fun toggleFollow(targetUserId: String, isFollowing: Boolean) {
        val currentUserId = auth.currentUser?.uid ?: return

        val currentUserRef = firestore.collection("users").document(currentUserId)
        val targetUserRef = firestore.collection("users").document(targetUserId)
        val followingRef = currentUserRef.collection("following").document(targetUserId)
        val followerRef = targetUserRef.collection("followers").document(currentUserId)

        firestore.runTransaction { transaction ->
            if (isFollowing) {
                // Désabonnement
                transaction.delete(followingRef)
                transaction.delete(followerRef)
                transaction.update(currentUserRef, "followingCount", FieldValue.increment(-1))
                transaction.update(targetUserRef, "followersCount", FieldValue.increment(-1))
            } else {
                // Abonnement
                val data = hashMapOf("timestamp" to Timestamp.now())
                transaction.set(followingRef, data)
                transaction.set(followerRef, data)
                transaction.update(currentUserRef, "followingCount", FieldValue.increment(1))
                transaction.update(targetUserRef, "followersCount", FieldValue.increment(1))
            }
        }.await()
    }

    // --- Commentaires ---

    // Récupère les commentaires d'un post en temps réel
    fun getComments(postId: String): Flow<List<Comment>> = callbackFlow {
        val listener = firestore.collection("posts").document(postId)
            .collection("comments")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val comments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<Comment>()?.copy(id = doc.id)
                } ?: emptyList()
                trySend(comments)
            }
        awaitClose { listener.remove() }
    }

    // Ajoute un commentaire et incrémente le compteur
    suspend fun addComment(postId: String, content: String) {
        val currentUser = auth.currentUser ?: return

        val comment = Comment(
            postId = postId,
            authorId = currentUser.uid,
            authorName = currentUser.displayName ?: "Utilisateur",
            content = content,
            timestamp = Timestamp.now()
        )

        val postRef = firestore.collection("posts").document(postId)

        try {
            firestore.runTransaction { transaction ->
                // 1. Créer le doc dans la sous-collection 'comments'
                val newCommentRef = postRef.collection("comments").document()
                transaction.set(newCommentRef, comment)

                // 2. Incrémenter le compteur sur le post parent
                transaction.update(postRef, "commentsCount", FieldValue.increment(1))
            }.await()
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Erreur lors de l'ajout du commentaire", e)
        }
    }

    // --- Historique & Events Utilisateur ---

    // --- Ajout pour la création d'événement ---
    suspend fun createEvent(event: Event): Result<String> {
        return try {
            // On laisse Firestore générer l'ID si celui de l'event est vide
            val docRef = if (event.id.isBlank()) {
                firestore.collection("events").document()
            } else {
                firestore.collection("events").document(event.id)
            }

            val finalEvent = event.copy(id = docRef.id)
            docRef.set(finalEvent).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Récupère tous les événements organisés par un utilisateur spécifique
    fun getUserEvents(userId: String): Flow<List<Event>> = callbackFlow {
        val listener = firestore.collection("events")
            .whereEqualTo("organizerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val events = snapshot?.documents?.mapNotNull { it.toObject<Event>(Event::class.java) } ?: emptyList()
                trySend(events)
            }
        awaitClose { listener.remove() }
    }

    // Récupérer un événement par son ID
    fun getEventById(eventId: String): Flow<Event?> = callbackFlow {
        val listener = firestore.collection("events").document(eventId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val event = snapshot?.toObject<Event>(Event::class.java)
                trySend(event)
            }
        awaitClose { listener.remove() }
    }

    // Supprimer un événement
    suspend fun deleteEvent(eventId: String) {
        try {
            firestore.collection("events").document(eventId).delete().await()
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Erreur lors de la suppression de l'événement", e)
        }
    }
/*
    suspend fun insertMockTrails() {
        val mockTrails = listOf(
            Trail(
            id = "trail_1",
            name = "Randonnée des Sapins",
            location = "Saguenay, Québec",
            distance = 12.5,
            duration = "4h 30min",
            difficulty = Difficulty.MODERATE,
            rating = 4.5f,
            reviewsCount = 234,
            description = "Magnifique sentier à travers la forêt de sapins avec vue panoramique.",
            latitude = 45.9237,
            longitude = 6.8694,
            tags = listOf("Forêt", "Vue panoramique", "Famille")
            ),
            Trail(
                id = "trail_2",
                name = "Randonnée des Pins",
                location = "Chicoutimi, Québec",
                distance = 30.2,
                duration = "8h 15min",
                difficulty = Difficulty.HARD,
                rating = 4.8f,
                reviewsCount = 156,
                description = "Sentier exigeant avec passages techniques et vues exceptionnelles.",
                latitude = 45.8992,
                longitude = 6.1294,
                tags = listOf("Technique", "Vue", "Expérimenté")
            ),
            Trail(
                id = "trail_3",
                name = "Sentier du ruisseau",
                location = "Chicoutimi, Québec",
                distance = 18.5,
                duration = "6h 00min",
                difficulty = Difficulty.EXPERT,
                rating = 4.9f,
                reviewsCount = 89,
                description = "Ascension mythique réservée aux randonneurs expérimentés.",
                latitude = 45.8326,
                longitude = 6.8652,
                tags = listOf("Haute montagne", "Glacier", "Expert")
            ),
            Trail(
                id = "trail_4",
                name = "Balade du Lac",
                location = "Montréal, Québec",
                distance = 5.2,
                duration = "2h 00min",
                difficulty = Difficulty.EASY,
                rating = 4.2f,
                reviewsCount = 456,
                description = "Promenade facile en bord de lac, idéale pour les familles.",
                latitude = 45.9000,
                longitude = 6.1167,
                tags = listOf("Lac", "Facile", "Famille")
            )
        )
        mockTrails.forEach { trail ->
            firestore.collection("trails").document(trail.id).set(trail).await()
        }
    }

    suspend fun insertMockPosts() {
        val mockPosts = listOf(
            Post(
                id = "post_1",
                authorId = "user_1",  // Assure-toi qu'un user existe
                authorName = "Helena",
                authorUsername = "helena_hiking",
                trailId = "trail_1",
                trailName = "Chemin des fleurs bleues",
                content = "Magnifique randonnée ce matin ! Les paysages étaient à couper le souffle 🏔️",
                likesCount = 21,
                commentsCount = 4,
                timestamp = Timestamp.now(),
                isLiked = false
            ),
            // Ajoute les autres
            Post(
                id = "post_2",
                authorId = "user_2",
                authorName = "Oscar",
                authorUsername = "oscar_mountain",
                trailId = "trail_2",
                trailName = "Mont Blanc Trail",
                content = "Une journée incroyable au Mont Blanc ! Conditions parfaites 🌞",
                likesCount = 45,
                commentsCount = 8,
                timestamp = Timestamp.now(),
                isLiked = true
            )
        )
        mockPosts.forEach { post ->
            firestore.collection("posts").document(post.id).set(post).await()
        }
    }

     */
}
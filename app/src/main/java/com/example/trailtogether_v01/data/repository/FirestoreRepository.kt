package com.example.trailtogether_v01.data.repository

import com.example.trailtogether_v01.data.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import com.google.firebase.Timestamp


class FirestoreRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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
        val listener = firestore.collection("trails").document(id)
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
        val listener = firestore.collection("posts")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING) // Assumant que timestamp est un champ Timestamp ou String sortable
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val posts = snapshot?.documents?.mapNotNull { it.toObject<Post>(Post::class.java) } ?: emptyList()
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
            timestamp = Timestamp.now() // Utiliser Timestamp pour meilleur tri
        )
        firestore.collection("posts").document(post.id).set(post).await()
        return Result.success(post)
    }

    // Ajoute une méthode pour like/unlike post (puisque likePost dans ViewModel est local, mais pour persistance)
    suspend fun toggleLikePost(postId: String, isLiked: Boolean) {
        val postRef = firestore.collection("posts").document(postId)
        firestore.runTransaction { transaction ->
            val post = transaction.get(postRef).toObject<Post>(Post::class.java) ?: return@runTransaction
            val newLikes = if (isLiked) post.likesCount - 1 else post.likesCount + 1
            transaction.update(postRef, "likesCount", newLikes, "isLiked", !isLiked) // isLiked est par post? Probablement besoin de likes par user séparé pour multi-users
        }.await()
        // Note: Pour une app multi-users, mieux avoir une subcollection "likes" par post pour tracker par user et éviter race conditions.
    }
    /*
    suspend fun insertMockTrails() {
        val mockTrails = listOf(
            Trail(
                id = "trail_1",
                name = "Randonnée des Sapins",
                location = "Chamonix, France",
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
            // Ajoute les autres trails de ton MockRepository
            Trail(
                id = "trail_2",
                name = "Randonnée des Pins",
                location = "Annecy, France",
                distance = 30.2,
                duration = "8h 15min",
                difficulty = Difficulty.HARD,
                rating = 4.8f,
                reviewsCount = 156,
                description = "Sentier exigeant avec passages techniques et vues exceptionnelles.",
                latitude = 45.8992,
                longitude = 6.1294,
                tags = listOf("Technique", "Vue", "Expérimenté")
            )
            // Etc.
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
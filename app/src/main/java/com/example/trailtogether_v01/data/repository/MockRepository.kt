package com.example.trailtogether_v01.data.repository

import com.example.trailtogether_v01.data.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.google.firebase.Timestamp


class MockRepository {

    private var currentUser: User? = null

    // Auth
    suspend fun login(email: String, password: String): Result<User> {
        delay(500) // Simulate network
        return if (email.isNotEmpty() && password.isNotEmpty()) {
            val user = getMockUser()
            currentUser = user
            Result.success(user)
        } else {
            Result.failure(Exception("Invalid credentials"))
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<User> {
        delay(500)
        val user = User(
            id = "user_${System.currentTimeMillis()}",
            name = name,
            username = email.substringBefore("@"),
            email = email,
            followersCount = 0,
            followingCount = 0,
            trailsCount = 0
        )
        currentUser = user
        return Result.success(user)
    }

    fun getCurrentUser(): User? = currentUser

    // Trails
    fun getTrails(): Flow<List<Trail>> = flow {
        delay(300)
        emit(getMockTrails())
    }

    fun getTrailById(id: String): Flow<Trail?> = flow {
        delay(200)
        emit(getMockTrails().find { it.id == id })
    }

    // Events
    fun getEvents(): Flow<List<Event>> = flow {
        delay(300)
        emit(getMockEvents())
    }

    fun getEventsByDate(date: String): Flow<List<Event>> = flow {
        delay(200)
        emit(getMockEvents().filter { it.date == date })
    }

    // Posts
    fun getPosts(): Flow<List<Post>> = flow {
        delay(300)
        emit(getMockPosts())
    }

    suspend fun createPost(trailId: String, content: String): Result<Post> {
        delay(500)
        val post = Post(
            id = "post_${System.currentTimeMillis()}",
            authorId = currentUser?.id ?: "unknown",
            authorName = currentUser?.name ?: "Anonymous",
            authorUsername = currentUser?.username ?: "anonymous",
            trailId = trailId,
            trailName = "Trail Name",
            content = content,
            likesCount = 0,
            commentsCount = 0,
            timestamp = Timestamp.now()
        )
        return Result.success(post)
    }

    // Mock Data
    private fun getMockUser() = User(
        id = "user_1",
        name = "Jean Dupont",
        username = "jeandupont",
        email = "jean@example.com",
        bio = "Passionné de randonnée 🥾",
        emergencyContact = "Marie Dupont",
        emergencyPhone = "+33 6 12 34 56 78",
        followersCount = 156,
        followingCount = 89,
        trailsCount = 12
    )

    private fun getMockTrails() = listOf(
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

    private fun getMockEvents() = listOf(
        Event(
            id = "event_1",
            trailId = "trail_1",
            trailName = "Randonnée des Sapins",
            organizerId = "user_2",
            organizerName = "Marie Martin",
            date = "2025-01-15",
            time = "13:00",
            duration = "4h 30min",
            distance = "12.5 km",
            difficulty = Difficulty.MODERATE,
            participantsCount = 8,
            maxParticipants = 15,
            description = "Sortie conviviale en groupe",
            meetingPoint = "Parking de la forêt"
        ),
        Event(
            id = "event_2",
            trailId = "trail_2",
            trailName = "Randonnée des Pins",
            organizerId = "user_3",
            organizerName = "Pierre Dubois",
            date = "2025-01-15",
            time = "09:00",
            duration = "8h 15min",
            distance = "30.2 km",
            difficulty = Difficulty.HARD,
            participantsCount = 5,
            maxParticipants = 10,
            description = "Pour randonneurs confirmés",
            meetingPoint = "Office de tourisme"
        ),
        Event(
            id = "event_3",
            trailId = "trail_3",
            trailName = "Sentier du Mont Blanc",
            organizerId = "user_1",
            organizerName = "Jean Dupont",
            date = "2025-01-20",
            time = "06:00",
            duration = "6h 00min",
            distance = "18.5 km",
            difficulty = Difficulty.EXPERT,
            participantsCount = 4,
            maxParticipants = 6,
            description = "Ascension technique",
            meetingPoint = "Refuge du Goûter"
        )
    )

    private fun getMockPosts() = listOf(
        Post(
            id = "post_1",
            authorId = "user_4",
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
        Post(
            id = "post_2",
            authorId = "user_5",
            authorName = "Oscar",
            authorUsername = "oscar_mountain",
            trailId = "trail_3",
            trailName = "Mont Blanc Trail",
            content = "Une journée incroyable au Mont Blanc ! Conditions parfaites 🌞",
            likesCount = 45,
            commentsCount = 8,
            timestamp = Timestamp.now(),
            isLiked = true
        ),
        Post(
            id = "post_3",
            authorId = "user_6",
            authorName = "Sophie",
            authorUsername = "sophie_trails",
            trailId = "trail_4",
            trailName = "Lac d'Annecy",
            content = "Balade familiale autour du lac, parfait pour un dimanche ! 🚶‍♀️",
            likesCount = 67,
            commentsCount = 12,
            timestamp = Timestamp.now(),
            isLiked = false
        )
    )
}
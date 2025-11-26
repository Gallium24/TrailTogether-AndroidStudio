package com.example.trailtogether_v01.data.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailtogether_v01.R
import com.example.trailtogether_v01.data.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Représente les différents états possibles de l'authentification.
 */
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * AuthViewModel.kt
 *
 * Gère toute la logique d'authentification des utilisateurs.
 *
 * États d'authentification (AuthState sealed class):
 * - Idle: État initial
 * - Loading: En cours d'authentification
 * - Success(user): Authentification réussie
 * - Error(message): Erreur d'authentification
 *
 * Fonctionnalités:
 * - Connexion email/mot de passe
 * - Inscription email/mot de passe avec validation
 * - Connexion Google Sign-In
 * - Déconnexion
 * - Vérification état de connexion au démarrage
 *
 * Méthodes principales:
 * - login(email, password): Connexion classique
 * - register(email, password, name): Inscription nouveau compte
 * - signInWithGoogle(credential): Connexion avec Google
 * - signOut(): Déconnexion
 * - checkAuthState(): Vérifie si utilisateur déjà connecté
 *
 * Gestion Google Sign-In:
 * - Configuration GoogleSignInClient dans le ViewModel
 * - Génération du Intent de connexion
 * - Traitement du résultat d'authentification
 *
 * Intégration Firebase:
 * - FirebaseAuth pour authentification
 * - FirestoreRepository pour création/récupération profil utilisateur
 * - Création automatique du profil lors de l'inscription
 *
 * StateFlow exposé:
 * - authState: État courant de l'authentification
 *
 * Utilisation:
 * - Observé par RootNavGraph pour navigation auth/main
 * - Utilisé par LoginScreen et RegisterScreen
 */

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = Firebase.auth
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance() // Ajouté
    private val googleSignInClient: GoogleSignInClient

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(application, gso)

        viewModelScope.launch {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                val user = userDoc.toObject(User::class.java)
                if (user != null) {
                    _currentUser.value = user
                    _authState.value = AuthState.Success(user)
                } else {
                    _authState.value = AuthState.Error("Utilisateur non trouvé dans la base de données")
                }
            } else {
                _authState.value = AuthState.Error("Aucun utilisateur connecté")
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email et mot de passe requis.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                    val user = userDoc.toObject(User::class.java)
                    if (user != null) {
                        _currentUser.value = user
                        _authState.value = AuthState.Success(user)
                    } else {
                        _authState.value = AuthState.Error("Utilisateur non trouvé")
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Échec de la connexion")
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Tous les champs sont requis.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user!!
                val user = User(
                    id = firebaseUser.uid,
                    name = name,
                    username = name, // Ou générer un username unique
                    email = firebaseUser.email!!,
                    followersCount = 0,
                    followingCount = 0,
                    trailsCount = 0
                )
                firestore.collection("users").document(firebaseUser.uid).set(user).await()
                _currentUser.value = user
                _authState.value = AuthState.Success(user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Échec de l'inscription")
            }
        }
    }

    fun logout() {
        auth.signOut()
        googleSignInClient.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    fun getGoogleSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun signInWithGoogle(data: Intent?) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(
                    GoogleSignIn.getSignedInAccountFromIntent(data).await().idToken,
                    null
                )
                val authResult = auth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user!!
                // Vérifier si l'utilisateur existe déjà dans Firestore
                val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                val user = if (userDoc.exists()) {
                    userDoc.toObject(User::class.java)!!
                } else {
                    // Créer un nouveau
                    User(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "Utilisateur Google",
                        username = firebaseUser.displayName ?: "Utilisateur Google",
                        email = firebaseUser.email!!,
                        followersCount = 0,
                        followingCount = 0,
                        trailsCount = 0
                    ).also {
                        firestore.collection("users").document(firebaseUser.uid).set(it).await()
                    }
                }
                _currentUser.value = user
                _authState.value = AuthState.Success(user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Échec de la connexion avec Google")
            }
        }
    }
}
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
import com.google.firebase.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = Firebase.auth
    private val googleSignInClient: GoogleSignInClient

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        // Configuration du client Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(application, gso)

        // Observer l'état de l'authentification Firebase au démarrage
        auth.currentUser?.let { firebaseUser ->
            _currentUser.value = User(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "Utilisateur",
                username = firebaseUser.displayName ?: "Utilisateur",
                email = firebaseUser.email ?: ""
            )
            _authState.value = AuthState.Success(_currentUser.value!!)
        }
    }

    // --- NOUVELLES FONCTIONS D'AUTHENTIFICATION ---

    // Connexion avec Email et Mot de passe
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
                    val user = User(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "Utilisateur",
                        username = firebaseUser.displayName ?: "Utilisateur",
                        email = firebaseUser.email!!
                    )
                    _currentUser.value = user
                    _authState.value = AuthState.Success(user)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Échec de la connexion")
            }
        }
    }

    // Inscription avec Email et Mot de passe
    fun register(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Tous les champs sont requis.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Étape 1: Créer l'utilisateur
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user!! // Non-null si succès

                // Note : Vous pourriez ajouter la mise à jour du nom d'affichage (displayName) ici
                // et stocker l'utilisateur dans Firestore/Realtime Database si nécessaire.

                val user = User(
                    id = firebaseUser.uid,
                    name = name,
                    username = name, // Utilise le nom fourni
                    email = firebaseUser.email!!
                )
                _currentUser.value = user
                _authState.value = AuthState.Success(user)

            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Échec de l'inscription")
            }
        }
    }

    // Déconnexion
    fun logout() {
        auth.signOut()
        googleSignInClient.signOut() // Important pour Google
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    // --- LOGIQUE POUR GOOGLE SIGN-IN ---

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
                val user = User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "Utilisateur Google",
                    username = firebaseUser.displayName ?: "Utilisateur Google",
                    email = firebaseUser.email!!
                )
                _currentUser.value = user
                _authState.value = AuthState.Success(user)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Échec de la connexion avec Google")
            }
        }
    }
}

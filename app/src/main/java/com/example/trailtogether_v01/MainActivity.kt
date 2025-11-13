package com.example.trailtogether_v01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.trailtogether_v01.data.viewmodel.AuthState
import com.example.trailtogether_v01.data.viewmodel.AuthViewModel
import com.example.trailtogether_v01.navigation.NavGraph
import com.example.trailtogether_v01.ui.screens.main.MainScreen
import com.example.trailtogether_v01.ui.theme.TrailTogetherTheme
import com.example.trailtogether_v01.utils.OsmdroidInitializer
/**
 * MainActivity est le point d'entrée unique de l'application.
 * Son rôle est de déterminer l'état d'authentification de l'utilisateur (connecté ou non)
 * et d'afficher le graphe de navigation approprié :
 * - Si l'utilisateur n'est pas connecté, affiche le NavGraph d'authentification (Login/Register).
 * - Si l'utilisateur est connecté, affiche le MainScreen qui contient la navigation principale de l'application.
 */

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Garde le splash screen actif tant qu'on ne sait pas si l'utilisateur est connecté
        installSplashScreen().setKeepOnScreenCondition {
            authViewModel.authState.value is AuthState.Idle
        }

        OsmdroidInitializer.init(this)

        /*
        val repository = FirestoreRepository()  // Instance du repo
        // Insertion des mocks (commente après le premier lancement !)
        lifecycleScope.launch {
            repository.insertMockTrails()
            repository.insertMockPosts()
        }*/


        setContent {
            TrailTogetherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authState by authViewModel.authState.collectAsState()
                    val navController = rememberNavController()

                    // --- LOGIQUE CENTRALE ---
                    // Affiche le bon écran en fonction de l'état de connexion
                    if (authState is AuthState.Success) {
                        // Utilisateur connecté : on affiche l'écran principal avec la barre de navigation
                        MainScreen(onLogout = { authViewModel.logout() })
                    } else {
                        // Utilisateur non connecté : on affiche le graphe de navigation pour l'authentification
                        NavGraph(
                            navController = navController,
                            authViewModel = authViewModel
                        )
                    }
                }
            }
        }
    }
}
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
import com.example.trailtogether_v01.navigation.RootNavGraph

import com.example.trailtogether_v01.ui.theme.TrailTogetherTheme
import com.example.trailtogether_v01.utils.OsmdroidInitializer
import org.osmdroid.config.Configuration

/**
 * MainActivity est le point d'entrée unique de l'application.
 * Elle gère l'état de l'authentification et la navigation entre les écrans.
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
        Configuration.getInstance().apply {
            userAgentValue = packageName
            load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
        }

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
                    RootNavGraph(
                        authViewModel = authViewModel,
                        authState = authState,
                        navController = navController
                    )
                }
            }
        }
    }
}
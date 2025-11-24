package com.example.trailtogether_v01

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.trailtogether_v01.data.viewmodel.AuthState
import com.example.trailtogether_v01.data.viewmodel.AuthViewModel
import com.example.trailtogether_v01.navigation.RootNavGraph
import com.example.trailtogether_v01.services.LocationService

import com.example.trailtogether_v01.ui.theme.TrailTogetherTheme
import com.example.trailtogether_v01.utils.OsmdroidInitializer
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint

/**
 * MainActivity est le point d'entrée unique de l'application.
 * Elle gère l'état de l'authentification et la navigation entre les écrans.
 */

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    private var onLocationPermissionResult: ((Boolean) -> Unit)? = null

    // Launcher pour demander la permission de localisation
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        onLocationPermissionResult?.invoke(granted)
    }

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
                var initialLocation by remember { mutableStateOf<GeoPoint?>(null) }
                var locationReady by remember { mutableStateOf(false) }

                // Demander la localisation au démarrage
                LaunchedEffect(Unit) {
                    initializeLocation { location ->
                        initialLocation = location
                        locationReady = true
                    }
                }

                if (locationReady) {
                    val authViewModel: AuthViewModel = viewModel()
                    val authState by authViewModel.authState.collectAsState()
                    val navController = rememberNavController()

                    RootNavGraph(
                        authViewModel = authViewModel,
                        authState = authState,
                        navController = navController,
                        initialLocation = initialLocation
                    )
                } else {
                    // Écran de chargement pendant la récupération de la position
                    LoadingScreen()
                }
            }
        }
    }

    private fun initializeLocation(onLocationReady: (GeoPoint) -> Unit) {
        // Vérifier si la permission a déjà été demandée
        if (LocationService.wasLocationPermissionAsked(this)) {
            // Permission déjà demandée, utiliser la position sauvegardée ou par défaut
            lifecycleScope.launch {
                val location = if (LocationService.hasLocationPermission(this@MainActivity)) {
                    LocationService.getCurrentLocation(this@MainActivity)
                } else {
                    LocationService.getSavedLocation(this@MainActivity)
                        ?: GeoPoint(48.4284, -71.0598) // Saguenay par défaut
                }
                onLocationReady(location)
            }
        } else {
            // Première fois : demander la permission
            onLocationPermissionResult = { granted ->
                lifecycleScope.launch {
                    val location = if (granted) {
                        val loc = LocationService.getCurrentLocation(this@MainActivity)
                        LocationService.saveLocationToPreferences(this@MainActivity, loc)
                        loc
                    } else {
                        GeoPoint(48.4284, -71.0598) // Saguenay par défaut
                    }
                    // Marquer que la permission a été demandée
                    val prefs = getSharedPreferences("trail_together_prefs", MODE_PRIVATE)
                    prefs.edit().putBoolean("location_permission_asked", true).apply()
                    onLocationReady(location)
                }
            }

            // Demander la permission
            requestLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }


    @Composable
    fun LoadingScreen() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}


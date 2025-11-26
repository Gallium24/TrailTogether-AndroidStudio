package com.example.trailtogether_v01.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.trailtogether_v01.data.viewmodel.AuthState
import com.example.trailtogether_v01.data.viewmodel.AuthViewModel
import com.example.trailtogether_v01.data.viewmodel.HomeViewModel
import com.example.trailtogether_v01.ui.components.BottomNavBar
import org.osmdroid.util.GeoPoint

/**
 * RootNavGraph.kt
 *
 * Graphe de navigation racine de l'application.
 * Détermine quel graphe afficher selon l'état d'authentification.
 *
 * Responsabilités:
 * - Décision Auth vs Main graph selon authState
 * - Initialisation de la position GPS initiale
 * - Gestion du state hoisting pour HomeViewModel
 *
 * Logique:
 * - Si authState est Success: Affiche MainNavGraph
 * - Sinon: Affiche AuthNavGraph
 *
 * Paramètres:
 * - authViewModel: ViewModel d'authentification (partagé)
 * - authState: État d'authentification courant
 * - navController: NavHostController racine
 * - initialLocation: Position GPS initiale (optionnel)
 *
 * Gestion de la position:
 * - Reçoit initialLocation depuis MainActivity
 * - Transmet à HomeViewModel via setInitialLocation()
 * - Déclenche le chargement initial des sentiers
 *
 * Utilisation:
 * - Point d'entrée de la navigation depuis MainActivity
 * - Crée le NavHost racine avec routes conditionnelles
 */

@Composable
fun RootNavGraph(
    authViewModel: AuthViewModel = viewModel(),
    authState: AuthState,
    navController: NavHostController,
    initialLocation: GeoPoint?
) {
    if (authState is AuthState.Success) {
        // Utilisateur connecté -> Affiche l'écran principal
        val navController = rememberNavController()
        val homeViewModel: HomeViewModel = viewModel()

        LaunchedEffect(initialLocation) {
            initialLocation?.let {
                homeViewModel.setInitialLocation(it)
            }
        }

        BottomNavBar(navController = navController) { innerPadding ->
            MainNavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                onLogout = { authViewModel.logout() }
            )
        }

    } else {
        // Utilisateur non connecté -> Affiche le flux d'authentification
        AuthNavGraph(
            navController = navController,
            authViewModel = authViewModel
        )
    }
}
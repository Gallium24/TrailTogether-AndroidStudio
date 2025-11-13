package com.example.trailtogether_v01.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.trailtogether_v01.data.viewmodel.AuthState
import com.example.trailtogether_v01.data.viewmodel.AuthViewModel
import com.example.trailtogether_v01.ui.components.BottomNavBar


/**
 * RootNavGraph est le graphe de navigation racine de l'application.
 * Il agit comme un aiguillage en fonction de l'état de connexion de l'utilisateur :
 * - Si l'utilisateur est connecté, il affiche MainScreen (qui a sa propre navigation interne).
 * - Sinon, il affiche AuthNavGraph (pour la connexion/inscription).
 */
@Composable
fun RootNavGraph(
    authViewModel: AuthViewModel = viewModel(),
    authState: AuthState,
    navController: NavHostController
) {
    if (authState is AuthState.Success) {
        // Utilisateur connecté -> Affiche l'écran principal
        val navController = rememberNavController()
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
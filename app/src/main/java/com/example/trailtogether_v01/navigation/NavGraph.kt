package com.example.trailtogether_v01.navigation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.trailtogether_v01.data.viewmodel.AuthViewModel
import com.example.trailtogether_v01.ui.screens.auth.LoginScreen
import com.example.trailtogether_v01.ui.screens.auth.RegisterScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            authViewModel.signInWithGoogle(result.data)
        }
    )

    val authState by authViewModel.authState.collectAsState()

    // Ce NavHost ne contient QUE les écrans de login et register
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route // L'écran de départ est toujours Login
    ) {
        // --- LOGIN ---
        composable(Screen.Login.route) {
            LoginScreen(
                authState = authState,
                onLoginClick = { email, password ->
                    authViewModel.login(email, password)
                },
                onGoogleSignInClick = {
                    googleSignInLauncher.launch(authViewModel.getGoogleSignInIntent())
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // --- REGISTER ---
        composable(Screen.Register.route) {
            RegisterScreen(
                authState = authState,
                onRegisterClick = { name, email, password ->
                    authViewModel.register(name, email, password)
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
    }
}
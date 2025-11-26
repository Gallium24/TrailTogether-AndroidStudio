package com.example.trailtogether_v01.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trailtogether_v01.R
import com.example.trailtogether_v01.data.viewmodel.AuthState
import com.example.trailtogether_v01.ui.theme.TrailGreen

/**
 * LoginScreen.kt
 *
 * Écran de connexion de l'application.
 *
 * Fonctionnalités:
 * - Connexion avec email et mot de passe
 * - Connexion avec Google Sign-In
 * - Navigation vers RegisterScreen
 * - Validation des champs
 * - Gestion des erreurs d'authentification
 *
 * Champs:
 * - Email: TextField avec validation
 * - Mot de passe: TextField avec masquage
 *
 * Boutons:
 * - "Se connecter": Connexion email/mot de passe
 * - "Se connecter avec Google": Connexion Google
 * - "Créer un compte": Navigation vers RegisterScreen
 *
 * Validation:
 * - Email: Format valide requis
 * - Mot de passe: Minimum 6 caractères
 * - Messages d'erreur affichés sous les champs
 *
 * États:
 * - Idle: Écran normal
 * - Loading: Affichage CircularProgressIndicator
 * - Success: Navigation automatique vers MainNavGraph
 * - Error: Affichage du message d'erreur
 *
 * Intégration:
 * - Observe authState de AuthViewModel
 * - Appelle login() ou signInWithGoogle()
 * - Firebase Authentication en arrière-plan
 *
 * Design:
 * - Logo en haut
 * - Formulaire centré
 * - Couleurs du thème TrailTogether
 *
 * Utilisation:
 * - Route par défaut de AuthNavGraph
 * - Premier écran affiché si non connecté
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    // Paramètres propres et corrigés, fournis par NavGraph
    authState: AuthState,
    onLoginClick: (String, String) -> Unit,
    onGoogleSignInClick: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isLoading = authState is AuthState.Loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.trailtogether_logo),
                contentDescription = "Logo de l'application",
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(bottom = 48.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mot de passe") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (authState is AuthState.Error) {
                Text(
                    text = authState.message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(38.dp))
            }

            Button(
                onClick = { onLoginClick(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TrailGreen),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Se connecter", fontSize = 16.sp)
                }
            }

            // 5. Lien vers l'inscription
            TextButton(onClick = onNavigateToRegister, enabled = !isLoading) {
                Text("Pas encore de compte ? S'inscrire")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Divider(modifier = Modifier.weight(1f))
                Text(text = " ou ", color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
                Divider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onGoogleSignInClick, // Appel de la bonne fonction
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google_logo), // Utilisation du logo Google
                    contentDescription = "Logo Google",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Continuer avec Google", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

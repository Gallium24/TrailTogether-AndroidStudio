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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trailtogether_v01.R
import com.example.trailtogether_v01.data.viewmodel.AuthState
import com.example.trailtogether_v01.ui.theme.TrailGreen

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
            // 1. Logo
            Image(
                painter = painterResource(id = R.drawable.trailtogether_logo),
                contentDescription = "Logo de l'application",
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(bottom = 48.dp)
            )

            // 2. Champs de saisie
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

            // 3. Affichage des erreurs
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
                // Espace réservé pour éviter que l'UI ne "saute" quand l'erreur apparaît
                Spacer(modifier = Modifier.height(38.dp))
            }

            // 4. Bouton de connexion
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

            // 6. Séparateur "ou"
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

            // 7. Bouton de connexion Google (corrigé)
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

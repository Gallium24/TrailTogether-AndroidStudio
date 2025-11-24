package com.example.trailtogether_v01.ui.screens.feed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trailtogether_v01.data.viewmodel.FeedViewModel
import com.example.trailtogether_v01.ui.components.PostCard
import com.example.trailtogether_v01.ui.theme.TrailGreen

/**
 * FeedScreen est la composante de l'écran du Feed.
 * @param onNavigateToCreatePost Une fonction lambda appelée lorsque l'utilisateur clique sur le bouton "Créer un post".
 * @param feedViewModel Le ViewModel du Feed.
 */
@Composable
fun FeedScreen(
    onNavigateToCreatePost: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    feedViewModel: FeedViewModel = viewModel()
) {
    val posts by feedViewModel.posts.collectAsState()
    val isLoading by feedViewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Feed",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNavigateToCreatePost) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Créer un post",
                        tint = TrailGreen
                    )
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(posts) { post ->
                    PostCard(
                        post = post,
                        onLikeClick = { feedViewModel.likePost(post.id) },
                        onUserClick = { userId -> onNavigateToUserProfile(userId) }
                    )
                }
            }
        }
    }
}
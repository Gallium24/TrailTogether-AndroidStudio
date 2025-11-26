package com.example.trailtogether_v01.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trailtogether_v01.data.models.Notification
import com.example.trailtogether_v01.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * NotificationViewModel.kt
 *
 * Gère les notifications in-app de l'utilisateur.
 *
 * Fonctionnalités:
 * - Chargement du flux de notifications (Flow réactif)
 * - Marquage d'une notification comme lue
 * - Marquage de toutes les notifications comme lues
 * - Compteur de notifications non lues
 *
 * StateFlows exposés:
 * - notifications: Liste des notifications de l'utilisateur
 * - unreadCount: Nombre de notifications non lues
 * - isLoading: Indicateur de chargement
 *
 * Méthodes:
 * - loadNotifications(): Charge les notifications (appelé automatiquement)
 * - markAsRead(notificationId): Marque une notification comme lue
 * - markAllAsRead(): Marque toutes les notifications comme lues
 * - clearNotifications(): Efface toutes les notifications (si implémenté)
 *
 * Logique du compteur:
 * - Calcul automatique depuis la liste de notifications
 * - Filtre les notifications où read == false
 * - Mis à jour réactivement via Flow
 *
 * Affichage:
 * - Badge sur BottomNavBar si unreadCount > 0
 * - Liste dans NotificationsScreen avec indicateur visuel
 * - Notifications triées par date (plus récentes en premier)
 *
 * Types de notifications:
 * - LIKE: Quelqu'un a liké votre post
 * - COMMENT: Quelqu'un a commenté votre post
 *
 * Utilisation:
 * - Utilisé par NotificationsScreen
 * - Badge unreadCount observé par BottomNavBar
 * - Clic sur notification → marque comme lue + navigation vers post
 */

class NotificationsViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.getNotifications(userId).collect { notificationList ->
                _notifications.value = notificationList
                _unreadCount.value = notificationList.count { !it.isRead }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            _notifications.value.filter { !it.isRead }.forEach { notification ->
                repository.markNotificationAsRead(notification.id)
            }
        }
    }
}
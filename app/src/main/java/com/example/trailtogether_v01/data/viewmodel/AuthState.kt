package com.example.trailtogether_v01.data.viewmodel

import com.example.trailtogether_v01.data.models.User

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}
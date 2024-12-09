package com.example.medisight.domain.usecase

import com.example.medisight.data.model.User

sealed class UserProfileState {
    object Loading : UserProfileState()
    data class Success(val user: User) : UserProfileState()
    data class Error(val message: String) : UserProfileState()
}
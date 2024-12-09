package com.example.medisight.data.response

sealed class UserResponse {
    data class Loading(val isLoading: Boolean) : UserResponse()
    data class Success(val message: String) : UserResponse()
    data class Error(val message: String) : UserResponse()
}
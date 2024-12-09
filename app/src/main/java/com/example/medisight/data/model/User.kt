package com.example.medisight.data.model

data class User(
    val id: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val dateOfBirth: String = "",
    val address: String = "",
    val profileImageUrl: String? = null
)
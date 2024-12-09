package com.example.medisight.data.repository

import com.example.medisight.domain.result.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository(private val firebaseAuth: FirebaseAuth) {

    suspend fun register(email: String, password: String): AuthResult {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Registration failed")
        }
    }

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Login failed")
        }
    }
}
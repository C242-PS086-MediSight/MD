package com.example.medisight.data.repository

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val contentResolver: ContentResolver
) {
    suspend fun uploadProfileImage(imageUri: Uri): String {
        val currentUser =
            auth.currentUser ?: throw IllegalStateException("User must be authenticated")
        require(imageUri != Uri.EMPTY) { "Invalid image URI" }

        return try {
            contentResolver.openInputStream(imageUri)?.use { inputStream ->
                require(inputStream.available() > 0) { "Image file does not exist or is empty" }
            }

            val filename = "${currentUser.uid}_${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference.child("profile_images/$filename")

            storageRef.putFile(imageUri).await()

            val downloadUrl = storageRef.downloadUrl.await()
            Log.d("UserRepository", "Image upload successful: $downloadUrl")

            updateUserProfileImageUrl(downloadUrl.toString())
            downloadUrl.toString()
        } catch (e: Exception) {
            Log.e("UserRepository", "Image upload failed", e)
            val errorMessage = when (e) {
                is java.net.UnknownHostException -> "Network error. Check your internet connection."
                is com.google.firebase.storage.StorageException -> "Storage upload failed. Check Firebase configuration."
                else -> "Failed to upload profile image: ${e.localizedMessage}"
            }
            throw Exception(errorMessage)
        }
    }

    private suspend fun updateUserProfileImageUrl(imageUrl: String) {
        val currentUser = auth.currentUser ?: return
        database.reference
            .child("users")
            .child(currentUser.uid)
            .child("profileImageUrl")
            .setValue(imageUrl)
            .await()
    }

    suspend fun getCurrentUserProfile(): Map<String, Any?>? = suspendCoroutine { continuation ->
        val userId = auth.currentUser?.uid ?: run {
            continuation.resume(null)
            return@suspendCoroutine
        }

        database.reference.child("users").child(userId).get()
            .addOnSuccessListener { snapshot ->
                val profileData = mutableMapOf<String, Any?>()
                snapshot.children.forEach { child ->
                    profileData[child.key!!] = child.value
                }
                continuation.resume(profileData)
            }
            .addOnFailureListener {
                continuation.resumeWithException(it)
            }
    }

    suspend fun updateUserProfile(updates: Map<String, Any?>) {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("No authenticated user")
        try {
            database.reference
                .child("users")
                .child(userId)
                .updateChildren(updates)
                .await()
            Log.d("UserRepository", "Profile updated successfully")
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to update profile", e)
            throw Exception("Failed to update profile: ${e.localizedMessage}")
        }
    }

    suspend fun saveUserProfile(
        fullName: String,
        phoneNumber: String,
        dateOfBirth: String,
        address: String,
        profileImageUrl: String? = null
    ) {
        val currentUser = auth.currentUser ?: throw IllegalStateException("No authenticated user")
        try {
            val currentProfile = getCurrentUserProfile() ?: emptyMap()

            val updates = mutableMapOf<String, Any?>()
            if (fullName.isNotBlank()) updates["fullName"] = fullName
            if (phoneNumber.isNotBlank()) updates["phoneNumber"] = phoneNumber
            if (dateOfBirth.isNotBlank()) updates["dateOfBirth"] = dateOfBirth
            if (address.isNotBlank()) updates["address"] = address
            if (!profileImageUrl.isNullOrBlank()) updates["profileImageUrl"] = profileImageUrl

            currentProfile.forEach { (key, value) ->
                if (!updates.containsKey(key) && value != null) {
                    updates[key] = value
                }
            }

            database.reference
                .child("users")
                .child(currentUser.uid)
                .updateChildren(updates)
                .await()

            Log.d("UserRepository", "Profile saved successfully")
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to save profile", e)
            throw Exception("Failed to save profile: ${e.localizedMessage}")
        }
    }
}
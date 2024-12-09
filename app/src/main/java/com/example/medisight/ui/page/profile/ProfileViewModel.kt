package com.example.medisight.ui.page.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisight.data.model.User
import com.example.medisight.data.repository.UserRepository
import com.example.medisight.domain.usecase.UserProfileState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfileState>(UserProfileState.Loading)
    val userProfile: StateFlow<UserProfileState> = _userProfile.asStateFlow()

    // Add this property to track the current profile image URL
    var currentProfileImageUrl: String? = null

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _userProfile.value = UserProfileState.Error("User not authenticated")
            return
        }

        viewModelScope.launch {
            try {
                database.reference.child("users").child(currentUser.uid)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val user = snapshot.getValue(User::class.java)
                            if (user != null) {
                                // Update the currentProfileImageUrl when fetching user profile
                                currentProfileImageUrl = user.profileImageUrl
                                _userProfile.value = UserProfileState.Success(user)
                            } else {
                                _userProfile.value = UserProfileState.Error("User data not found")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            _userProfile.value = UserProfileState.Error(error.message)
                        }
                    })
            } catch (e: Exception) {
                _userProfile.value = UserProfileState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateUserProfile(user: User) {
        viewModelScope.launch {
            try {
                val currentUser =
                    auth.currentUser ?: throw IllegalStateException("No authenticated user")
                database.reference.child("users").child(currentUser.uid).setValue(user)
                    .addOnSuccessListener {
                        _userProfile.value = UserProfileState.Success(user)
                    }
                    .addOnFailureListener {
                        _userProfile.value = UserProfileState.Error("Failed to update profile")
                    }
            } catch (e: Exception) {
                _userProfile.value = UserProfileState.Error(e.message ?: "Update failed")
            }
        }
    }
}
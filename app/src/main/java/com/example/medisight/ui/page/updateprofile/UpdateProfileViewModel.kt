package com.example.medisight.ui.page.updateprofile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisight.data.repository.UserRepository
import com.example.medisight.domain.usecase.UpdateProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpdateProfileViewModel(private val userRepository: UserRepository) : ViewModel() {
    private var currentImageUri: Uri? = null
    private var currentProfileData: Map<String, Any?> = mapOf()
    private val _updateState = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Idle)
    val updateState: StateFlow<UpdateProfileState> = _updateState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                currentProfileData = userRepository.getCurrentUserProfile() ?: mapOf()
            } catch (e: Exception) {
                _updateState.value = UpdateProfileState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    fun updateProfileImage(uri: Uri) {
        currentImageUri = uri
    }

    fun saveProfile(
        fullName: String,
        phoneNumber: String,
        dateOfBirth: String,
        address: String
    ) {
        viewModelScope.launch {
            _updateState.value = UpdateProfileState.Loading

            try {
                val updates = mutableMapOf<String, Any?>()

                if (fullName.isNotBlank()) {
                    updates["fullName"] = fullName
                } else {
                    currentProfileData["fullName"]?.let { updates["fullName"] = it }
                }

                if (phoneNumber.isNotBlank()) {
                    updates["phoneNumber"] = phoneNumber
                } else {
                    currentProfileData["phoneNumber"]?.let { updates["phoneNumber"] = it }
                }

                if (dateOfBirth.isNotBlank()) {
                    updates["dateOfBirth"] = dateOfBirth
                } else {
                    currentProfileData["dateOfBirth"]?.let { updates["dateOfBirth"] = it }
                }

                if (address.isNotBlank()) {
                    updates["address"] = address
                } else {
                    currentProfileData["address"]?.let { updates["address"] = it }
                }

                currentImageUri?.let { uri ->
                    val imageUrl = userRepository.uploadProfileImage(uri)
                    updates["profileImageUrl"] = imageUrl
                } ?: run {
                    currentProfileData["profileImageUrl"]?.let {
                        updates["profileImageUrl"] = it
                    }
                }

                userRepository.updateUserProfile(updates)
                _updateState.value = UpdateProfileState.Success
            } catch (e: Exception) {
                _updateState.value = UpdateProfileState.Error(e.message ?: "Update failed")
            }
        }
    }
}
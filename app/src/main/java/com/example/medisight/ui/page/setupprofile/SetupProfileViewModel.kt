package com.example.medisight.ui.page.setupprofile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisight.data.repository.UserRepository
import com.example.medisight.data.response.UserResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SetupProfileViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _userResponse = MutableStateFlow<UserResponse>(UserResponse.Loading(false))
    val userResponse = _userResponse.asStateFlow()

    private var _profileImageUrl: String? = null
    val profileImageUrl: String?
        get() = _profileImageUrl

    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            try {
                _userResponse.value = UserResponse.Loading(true)
                _profileImageUrl = userRepository.uploadProfileImage(imageUri)
                _userResponse.value = UserResponse.Success("Image uploaded successfully")
            } catch (e: Exception) {
                _userResponse.value = UserResponse.Error(e.message ?: "Image upload failed")
            }
        }
    }

    fun saveUserProfile(
        fullName: String,
        phoneNumber: String,
        dateOfBirth: String,
        address: String,
        profileImageUri: Uri? = null
    ) {
        viewModelScope.launch {
            try {
                _userResponse.value = UserResponse.Loading(true)
                userRepository.saveUserProfile(
                    fullName,
                    phoneNumber,
                    dateOfBirth,
                    address,
                    _profileImageUrl
                )
                _userResponse.value = UserResponse.Success("Profile saved successfully")
            } catch (e: Exception) {
                _userResponse.value = UserResponse.Error(e.message ?: "Profile save failed")
            }
        }
    }
}
package com.example.medisight.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.medisight.data.repository.UserRepository
import com.example.medisight.ui.page.updateprofile.UpdateProfileViewModel

class UpdateProfileViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UpdateProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UpdateProfileViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
package com.example.medisight.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.medisight.data.repository.UserRepository
import com.example.medisight.ui.page.setupprofile.SetupProfileViewModel

class SetupProfileViewModelFactory(
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SetupProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SetupProfileViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
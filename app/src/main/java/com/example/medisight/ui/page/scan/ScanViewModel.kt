package com.example.medisight.ui.page.scan

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ScanViewModel : ViewModel() {
    private val _currentPhotoUri = MutableLiveData<Uri?>()
    val currentPhotoUri: LiveData<Uri?> = _currentPhotoUri

    private val _isAnalyzeButtonEnabled = MutableLiveData(false)
    val isAnalyzeButtonEnabled: LiveData<Boolean> = _isAnalyzeButtonEnabled

    fun updateCurrentPhotoUri(uri: Uri?) {
        _currentPhotoUri.value = uri
        _isAnalyzeButtonEnabled.value = uri != null
    }
}
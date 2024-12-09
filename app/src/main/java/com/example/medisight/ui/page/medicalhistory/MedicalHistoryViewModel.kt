package com.example.medisight.ui.page.medicalhistory

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisight.data.model.ScanHistory
import com.example.medisight.data.repository.MedicalHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MedicalHistoryViewModel : ViewModel() {
    private val repository = MedicalHistoryRepository()
    private val _histories = MutableStateFlow<List<ScanHistory>>(emptyList())
    val histories: StateFlow<List<ScanHistory>> = _histories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getHistoryForUser()
                .catch { e ->
                    Log.e("MedicalResumeViewModel", "Error fetching histories", e)
                }
                .collect {
                    _histories.value = it
                }
        }
    }

    fun deleteHistory(historyId: String) {
        _isLoading.value = true
        repository.deleteHistory(historyId) { success ->
            _isLoading.value = false
        }
    }
}
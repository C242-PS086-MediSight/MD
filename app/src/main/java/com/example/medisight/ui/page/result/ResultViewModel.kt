package com.example.medisight.ui.page.result

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisight.data.model.ClassificationLabel
import com.example.medisight.data.model.Obat
import com.example.medisight.data.model.Wound
import com.example.medisight.data.network.ApiConfig
import com.example.medisight.data.network.ApiService
import com.example.medisight.domain.result.ClassificationResult
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResultViewModel : ViewModel() {
    private val _imageUri = MutableLiveData<Uri>()
    val imageUri: LiveData<Uri> = _imageUri

    private val _classificationResult = MutableLiveData<ClassificationResult>()
    val classificationResult: LiveData<ClassificationResult> = _classificationResult

    private val _woundHandlingInfo = MutableLiveData<Wound?>()
    val woundHandlingInfo: LiveData<Wound?> = _woundHandlingInfo

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _medicineList = MutableLiveData<List<Obat>>()
    val medicineList: LiveData<List<Obat>> = _medicineList

    private val apiService = ApiConfig.getRetrofitInstance().create(ApiService::class.java)

    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
    }

    fun setClassificationResult(results: FloatArray?, labels: List<ClassificationLabel>) {
        viewModelScope.launch {
            _isLoading.value = true
            results?.let { probabilities ->
                val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
                val maxProbability = probabilities[maxIndex]
                val label = labels.getOrNull(maxIndex)

                label?.let {
                    _classificationResult.value = ClassificationResult(it, maxProbability)
                    if (it.label != "Normal") {
                        fetchWoundHandlingInfo(getWoundIdForLabel(it.label))
                        fetchMedicineForWound(it.label)
                    } else {
                        _woundHandlingInfo.value = null
                        _medicineList.value = emptyList()
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    private fun fetchMedicineForWound(woundType: String) {
        val medicineIds = when (woundType) {
            "Burns" -> listOf(1, 2, 3, 4)
            "Abrasions" -> listOf(5, 6, 7)
            "Cut" -> listOf(8, 9)
            "Bruises" -> listOf(10, 11)
            else -> emptyList()
        }

        viewModelScope.launch {
            try {
                val medicines = mutableListOf<Obat>()
                var completedCalls = 0
                val totalCalls = medicineIds.size

                for (id in medicineIds) {
                    apiService.getObatDetail(id).enqueue(object : Callback<Obat> {
                        override fun onResponse(call: Call<Obat>, response: Response<Obat>) {
                            synchronized(medicines) {
                                if (response.isSuccessful) {
                                    response.body()?.let { medicines.add(it) }
                                }
                                completedCalls++

                                if (completedCalls == totalCalls) {
                                    val sortedMedicines = medicines.sortedBy { it.id }
                                    _medicineList.postValue(sortedMedicines)
                                    Log.d(
                                        "MedicineFetch",
                                        "Fetched ${medicines.size} medicines: ${medicines.map { it.id }}"
                                    )
                                }
                            }
                        }

                        override fun onFailure(call: Call<Obat>, t: Throwable) {
                            synchronized(medicines) {
                                Log.e("MedicineFetch", "Failed to fetch medicine id: $id", t)
                                completedCalls++
                                if (completedCalls == totalCalls) {
                                    val sortedMedicines = medicines.sortedBy { it.id }
                                    _medicineList.postValue(sortedMedicines)
                                }
                            }
                        }
                    })
                }
            } catch (e: Exception) {
                Log.e("MedicineFetch", "Error in medicine fetch", e)
                _medicineList.postValue(emptyList())
            }
        }
    }

    private fun getWoundIdForLabel(label: String): Int? {
        return when (label) {
            "Abrasions" -> 1
            "Bruises" -> 4
            "Burns" -> 2
            "Cut" -> 3
            else -> null
        }
    }

    private suspend fun fetchWoundHandlingInfo(woundId: Int?) {
        _isLoading.value = true
        try {
            woundId?.let {
                val response = apiService.getWoundById(it)
                if (response.isSuccessful) {
                    _woundHandlingInfo.value = response.body()
                }
            }
        } catch (e: Exception) {
            Log.e("ResultViewModel", "Error fetching wound info", e)
        } finally {
            _isLoading.value = false
        }
    }
}
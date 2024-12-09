package com.example.medisight.ui.page.detailarticle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.medisight.data.model.Artikel
import com.example.medisight.data.response.ArtikelDetailResponse
import com.example.medisight.data.network.ApiConfig
import com.example.medisight.data.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArtikelDetailViewModel : ViewModel() {
    private val _artikel = MutableLiveData<Artikel>()
    val artikel: LiveData<Artikel> = _artikel

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchArtikelDetail(id: Int) {
        _isLoading.value = true
        val apiService = ApiConfig.getRetrofitInstance().create(ApiService::class.java)

        apiService.getArtikelDetail(id).enqueue(object : Callback<ArtikelDetailResponse> {
            override fun onResponse(
                call: Call<ArtikelDetailResponse>,
                response: Response<ArtikelDetailResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    response.body()?.let {
                        _artikel.value = it.article
                    } ?: run {
                        _error.value = "Article not found"
                    }
                } else {
                    _error.value = "Failed to fetch article details"
                }
            }

            override fun onFailure(call: Call<ArtikelDetailResponse>, t: Throwable) {
                _isLoading.value = false
                _error.value = t.message ?: "Unknown error occurred"
            }
        })
    }
}
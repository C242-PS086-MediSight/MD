package com.example.medisight.ui.page.article

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.medisight.data.model.Artikel
import com.example.medisight.data.response.ArtikelResponse
import com.example.medisight.data.network.ApiConfig
import com.example.medisight.data.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArtikelViewModel : ViewModel() {
    private val _artikelList = MutableLiveData<List<Artikel>>()
    val artikelList: LiveData<List<Artikel>> = _artikelList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchArtikels() {
        _isLoading.value = true
        val apiService = ApiConfig.getRetrofitInstance().create(ApiService::class.java)

        apiService.getArtikelList().enqueue(object : Callback<ArtikelResponse> {
            override fun onResponse(
                call: Call<ArtikelResponse>,
                response: Response<ArtikelResponse>
            ) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _artikelList.value = response.body()?.articles
                } else {
                    _error.value = "Failed to fetch articles"
                }
            }

            override fun onFailure(call: Call<ArtikelResponse>, t: Throwable) {
                _isLoading.value = false
                _error.value = t.message ?: "Unknown error occurred"
            }
        })
    }
}
package com.example.medisight.ui.page.detailmedicine

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.medisight.data.model.Obat
import com.example.medisight.data.network.ApiConfig
import com.example.medisight.data.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ObatDetailViewModel : ViewModel() {
    private val _obat = MutableLiveData<Obat>()
    val obat: LiveData<Obat> = _obat

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val apiService: ApiService =
        ApiConfig.getRetrofitInstance().create(ApiService::class.java)

    fun getObatDetail(id: Int) {
        _isLoading.value = true
        apiService.getObatDetail(id).enqueue(object : Callback<Obat> {
            override fun onResponse(call: Call<Obat>, response: Response<Obat>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _obat.value = response.body()
                } else {
                    _error.value = "Error: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<Obat>, t: Throwable) {
                _isLoading.value = false
                _error.value = t.message
            }
        })
    }
}
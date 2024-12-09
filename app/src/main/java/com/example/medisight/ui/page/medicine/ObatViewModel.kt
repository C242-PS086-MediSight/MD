package com.example.medisight.ui.page.medicine

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.medisight.data.model.Obat
import com.example.medisight.data.network.ApiConfig
import com.example.medisight.data.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ObatViewModel : ViewModel() {
    private val _obatList = MutableLiveData<List<Obat>>()
    val obatList: LiveData<List<Obat>> = _obatList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var originalList: List<Obat> = emptyList()

    fun fetchObatList() {
        _isLoading.value = true
        val apiService = ApiConfig.getRetrofitInstance().create(ApiService::class.java)
        apiService.getObatList().enqueue(object : Callback<List<Obat>> {
            override fun onResponse(call: Call<List<Obat>>, response: Response<List<Obat>>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    response.body()?.let {
                        _obatList.value = it
                        originalList = it
                    }
                }
            }

            override fun onFailure(call: Call<List<Obat>>, t: Throwable) {
                _isLoading.value = false
            }
        })
    }

    fun searchObat(query: String) {
        val filteredList = originalList.filter {
            it.nama_obat.contains(query, ignoreCase = true)
        }
        _obatList.value = filteredList
    }
}
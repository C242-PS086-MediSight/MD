package com.example.medisight.ui.page.healthservices

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.medisight.data.model.Faskes
import com.example.medisight.data.network.ApiConfig
import com.example.medisight.data.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FaskesViewModel(application: Application) : AndroidViewModel(application) {

    private val _faskesList = MutableLiveData<List<Faskes>>()
    val faskesList: LiveData<List<Faskes>> = _faskesList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var originalFaskesList = listOf<Faskes>()

    fun getFaskesList() {
        _isLoading.value = true
        val apiService = ApiConfig.getRetrofitInstance().create(ApiService::class.java)
        apiService.getFaskesList().enqueue(object : Callback<List<Faskes>> {
            override fun onResponse(call: Call<List<Faskes>>, response: Response<List<Faskes>>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val fetchedList = response.body() ?: listOf()
                    originalFaskesList = fetchedList
                    _faskesList.postValue(fetchedList)
                }
            }

            override fun onFailure(call: Call<List<Faskes>>, t: Throwable) {
                _isLoading.value = false
            }
        })
    }

    fun searchFaskes(query: String) {
        val filteredList = originalFaskesList.filter { faskes ->
            faskes.nama_faskes.contains(query, ignoreCase = true)
        }
        _faskesList.value = filteredList
    }
}
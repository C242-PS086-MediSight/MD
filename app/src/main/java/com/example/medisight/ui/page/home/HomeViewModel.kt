package com.example.medisight.ui.page.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisight.data.model.Artikel
import com.example.medisight.data.response.ArtikelResponse
import com.example.medisight.data.model.Obat
import com.example.medisight.data.model.User
import com.example.medisight.data.network.ApiConfig
import com.example.medisight.data.network.ApiService
import com.example.medisight.data.repository.UserRepository
import com.example.medisight.domain.usecase.UserProfileState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : ViewModel() {

    private val _hotArticles = MutableLiveData<List<Artikel>>()
    val hotArticles: LiveData<List<Artikel>> = _hotArticles

    private val _medicines = MutableLiveData<List<Obat>>()
    val medicines: LiveData<List<Obat>> = _medicines

    private val _userProfile = MutableStateFlow<UserProfileState>(UserProfileState.Loading)
    val userProfile: StateFlow<UserProfileState> = _userProfile.asStateFlow()

    init {
        fetchHotArticles()
        fetchMedicines()
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _userProfile.value = UserProfileState.Error("User not authenticated")
            return
        }

        viewModelScope.launch {
            try {
                database.reference.child("users").child(currentUser.uid)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val user = snapshot.getValue(User::class.java)
                            if (user != null) {
                                _userProfile.value = UserProfileState.Success(user)
                            } else {
                                _userProfile.value = UserProfileState.Error("User data not found")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            _userProfile.value = UserProfileState.Error(error.message)
                        }
                    })
            } catch (e: Exception) {
                _userProfile.value = UserProfileState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun fetchHotArticles() {
        val apiService = ApiConfig.getRetrofitInstance().create(ApiService::class.java)
        apiService.getArtikelList().enqueue(object : retrofit2.Callback<ArtikelResponse> {
            override fun onResponse(
                call: retrofit2.Call<ArtikelResponse>,
                response: retrofit2.Response<ArtikelResponse>
            ) {
                if (response.isSuccessful) {
                    _hotArticles.value = response.body()?.articles
                }
            }

            override fun onFailure(call: retrofit2.Call<ArtikelResponse>, t: Throwable) {
                // Handle error
            }
        })
    }

    private fun fetchMedicines() {
        val apiService = ApiConfig.getRetrofitInstance().create(ApiService::class.java)
        apiService.getObatList().enqueue(object : retrofit2.Callback<List<Obat>> {
            override fun onResponse(
                call: retrofit2.Call<List<Obat>>,
                response: retrofit2.Response<List<Obat>>
            ) {
                if (response.isSuccessful) {
                    _medicines.value = response.body()
                }
            }

            override fun onFailure(call: retrofit2.Call<List<Obat>>, t: Throwable) {
            }
        })
    }
}
package com.example.medisight.data.network

import com.example.medisight.data.response.ArtikelDetailResponse
import com.example.medisight.data.response.ArtikelResponse
import com.example.medisight.data.model.Faskes
import com.example.medisight.data.model.Obat
import com.example.medisight.data.model.Wound
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("luka/{id}")
    suspend fun getWoundById(@Path("id") id: Int): Response<Wound>

    @GET("/faskes")
    fun getFaskesList(): Call<List<Faskes>>

    @GET("/obat")
    fun getObatList(): Call<List<Obat>>

    @GET("/article")
    fun getArtikelList(): Call<ArtikelResponse>

    @GET("/article/{id}")
    fun getArtikelDetail(@Path("id") id: Int): Call<ArtikelDetailResponse>

    @GET("/obat/{id}")
    fun getObatDetail(@Path("id") id: Int): Call<Obat>
}
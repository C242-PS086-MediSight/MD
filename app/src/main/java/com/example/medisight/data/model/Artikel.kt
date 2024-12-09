package com.example.medisight.data.model

data class Artikel(
    val id: Int,
    val title: String,
    val foto_article: String,
    val content: String,
    val author:String,
    val editor: String,
    val source: String,
    val published_date: String,
    val created_at: String
)
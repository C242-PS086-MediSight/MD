package com.example.medisight.data.model

import com.example.medisight.ui.enums.MessageType

data class Message(
    val id: String = System.currentTimeMillis().toString(),
    val text: String,
    val type: MessageType,
    val timestamp: Long = System.currentTimeMillis()
)
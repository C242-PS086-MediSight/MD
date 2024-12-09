package com.example.medisight.ui.page.chatbot

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medisight.data.model.Message
import com.example.medisight.ui.enums.MessageType
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

class ChatBotViewModel : ViewModel() {
    private val _messages = MutableLiveData<MutableList<Message>>(mutableListOf())
    val messages: LiveData<MutableList<Message>> = _messages

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val apiKey = "AIzaSyCXsbCsljfCyf2HMhNAH4fEVXn_0CVHhrY"

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-pro",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.7f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 8192
                candidateCount = 1
                responseMimeType = "text/plain"
            }
        )
    }

    private val chatHistory = mutableListOf(
        content("user") { text("Kamu adalah asisten medis untuk memberikan informasi kesehatan dengan bahasa indonesia yang baik dan ramah.") },
        content("model") { text("Baik, saya siap membantu Anda dengan informasi kesehatan. Saya akan memberikan jawaban dengan bahasa indonesia yang jelas dan mudah dipahami.") }
    )

    private fun isValidQuestion(question: String): Boolean {
        val validQuestions = listOf(
            "Rekomendasi obat herbal untuk luka ringan",
            "Informasi tentang obat herbal untuk luka ringan (cut, bruises, abrasion, burns)",
            "Apa itu Luka Ringan?",
            "Penjelasan dasar tentang luka ringan dan karakteristiknya",
            "Tanda infeksi?",
            "Informasi tentang gejala infeksi pada luka ringan",
            "Jenis luka ringan?",
            "Penjelasan berbagai jenis luka ringan (cut, bruises, abrasion, burns)"
        )
        return validQuestions.any { it.contains(question, ignoreCase = true) }

    }

    fun sendCardMessage(title: String, description: String) {
        val cardMessage = Message(
            text = "$title|$description",
            type = MessageType.CARD
        )
        val currentMessages = _messages.value ?: mutableListOf()
        currentMessages.add(cardMessage)
        _messages.value = currentMessages

        sendAIRequest(description)
    }

    fun sendUserMessage(text: String) {
        val userMessage = Message(text = text, type = MessageType.USER)
        val currentMessages = _messages.value ?: mutableListOf()
        currentMessages.add(userMessage)
        _messages.value = currentMessages

        if (text.contains("luka", ignoreCase = true) && isValidQuestion(text)) {
            sendAIRequest(text)
        } else {
            val botMessage = Message(
                text = "Maaf, saya hanya dapat menjawab pertanyaan tentang luka ringan yang terdaftar.",
                type = MessageType.BOT
            )
            currentMessages.add(botMessage)
            _messages.value = currentMessages
        }
    }

    private fun sendAIRequest(prompt: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val aiResponse = generateAIResponse(prompt)
                val botMessage = Message(text = aiResponse, type = MessageType.BOT)

                val currentMessages = _messages.value ?: mutableListOf()
                currentMessages.add(botMessage)
                _messages.value = currentMessages

                _isLoading.value = false
            } catch (e: Exception) {
                Log.e("ChatBotViewModel", "Error generating AI response", e)
                val errorMessage = Message(
                    text = "Maaf, terjadi kesalahan. Silakan coba lagi.",
                    type = MessageType.BOT
                )

                val currentMessages = _messages.value ?: mutableListOf()
                currentMessages.add(errorMessage)
                _messages.value = currentMessages

                _isLoading.value = false
            }
        }
    }

    private suspend fun generateAIResponse(prompt: String): String {
        return try {
            val chat = generativeModel.startChat(chatHistory)

            val response = chat.sendMessage(prompt)

            response.text ?: "Maaf, saya tidak dapat memberikan jawaban."
        } catch (e: Exception) {
            Log.e("ChatBotViewModel", "API Request Failed", e)
            "Maaf, terjadi kesalahan dalam memproses permintaan. ${e.localizedMessage}"
        }
    }
}
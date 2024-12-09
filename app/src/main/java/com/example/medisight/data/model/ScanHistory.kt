package com.example.medisight.data.model

data class ScanHistory(
    val id: String = "",
    val userId: String = "",
    val imageUrl: String = "",
    val scanResult: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val confidence: Float = 0f
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "imageUrl" to imageUrl,
            "scanResult" to scanResult,
            "timestamp" to timestamp,
            "confidence" to confidence
        )
    }
}
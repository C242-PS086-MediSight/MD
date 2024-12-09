package com.example.medisight.domain.result

import com.example.medisight.data.model.ClassificationLabel

data class ClassificationResult(
    val label: ClassificationLabel,
    val confidence: Float
)
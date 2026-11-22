package com.example.app.domain.models.post_ocr

data class PostOCRGroundTruth(
    val ocrText: String,
    val ocrAccuracy: Float,
    val expectedText: String,
)

package com.example.app.domain.models.post_ocr

data class PostOCRBenchmarkResult(
    val ocrText: String,
    val correctedText: String,
    val expectedText: String,
    val latencyMs: Long,
    val ocrAccuracy: Float,
    val currentAccuracy: Float,
)
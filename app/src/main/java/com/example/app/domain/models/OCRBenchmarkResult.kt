package com.example.app.domain.models

data class OCRBenchmarkResult(
    val result: Pair<String, String>,   // first: expectedText, second: extractedText
    val latencyMs: Long,
    val accuracy: Float,
)
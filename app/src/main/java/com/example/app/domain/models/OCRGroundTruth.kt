package com.example.app.domain.models

data class OCRGroundTruth(
    val assetPath: String,
    val expectedText: String,
)

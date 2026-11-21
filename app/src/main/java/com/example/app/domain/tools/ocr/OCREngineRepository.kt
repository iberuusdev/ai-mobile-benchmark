package com.example.app.domain.tools.ocr

interface OCREngineRepository {
    suspend fun recognizeText(assetPath: String): String
}
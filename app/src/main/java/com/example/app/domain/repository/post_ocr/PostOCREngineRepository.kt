package com.example.app.domain.repository.post_ocr

interface PostOCREngineRepository {
    suspend fun correctText(ocrText: String): String

    suspend fun close()
}
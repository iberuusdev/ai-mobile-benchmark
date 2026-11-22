package com.example.app.domain.models.ocr

enum class OCREngine(val displayName: String) {
    ML_KIT("Google ML Kit"),
    TESSERACT("Tesseract"),
}
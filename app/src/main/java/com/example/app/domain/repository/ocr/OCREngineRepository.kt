package com.example.app.domain.repository.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage

interface OCREngineRepository {
    suspend fun recognizeText(
        image: InputImage,
        bitmapImage: Bitmap,
    ): String

    suspend fun close()
}
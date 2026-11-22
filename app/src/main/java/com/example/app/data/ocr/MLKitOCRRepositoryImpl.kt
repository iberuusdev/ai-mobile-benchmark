package com.example.app.data.ocr

import android.graphics.Bitmap
import com.example.app.domain.repository.ocr.OCREngineRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MLKitOCRRepositoryImpl : OCREngineRepository {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun recognizeText(
        image: InputImage,
        bitmapImage: Bitmap,
    ): String {
        return suspendCancellableCoroutine { continuation ->
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        if (continuation.isActive) continuation.resume(visionText.text)
                    }
                    .addOnFailureListener { exception ->
                        if (continuation.isActive) continuation.resumeWithException(exception)
                    }
            }
    }

    override suspend fun close() {
        recognizer.close()
    }
}
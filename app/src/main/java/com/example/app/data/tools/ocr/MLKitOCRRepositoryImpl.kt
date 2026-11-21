package com.example.app.data.tools.ocr

import android.content.Context
import android.graphics.BitmapFactory
import com.example.app.domain.tools.ocr.OCREngineRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.AutoCloseable
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MLKitOCRRepositoryImpl(
    context: Context,
) : OCREngineRepository, AutoCloseable {

    private val appContext = context.applicationContext
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun recognizeText(assetPath: String): String {
        val bitmap = appContext.assets.open(assetPath).use { stream ->
            BitmapFactory.decodeStream(stream)
        } ?: throw IllegalArgumentException("Could not open image: $assetPath")

        val image = InputImage.fromBitmap(bitmap, 0)
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

    override fun close() {
        recognizer.close()
    }
}
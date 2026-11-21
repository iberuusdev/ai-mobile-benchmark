package com.example.app.data.tools.ocr

import android.content.Context
import android.graphics.Bitmap
import com.example.app.domain.repository.ocr.OCREngineRepository
import com.google.mlkit.vision.common.InputImage
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File

class TesseractRepositoryImpl(
    context: Context,
) : OCREngineRepository {
    private val appContext = context.applicationContext
    private val tess: TessBaseAPI = TessBaseAPI()

    init {
        val dataPath = File(appContext.filesDir, "tesseract").absolutePath
        val tessDataDir = File(dataPath, "tessdata")
        tessDataDir.mkdirs()

        val trainedData = File(tessDataDir, "spa.traineddata")
        if (!trainedData.exists()) {
            context.assets.open("tessdata/spa.traineddata").use { input ->
                trainedData.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        if (!tess.init(dataPath, "spa")) {
            throw IllegalStateException(
                "Couldn't initialise OCR training for Tesseract correctly."
            )
        }
    }

    override suspend fun recognizeText(
        image: InputImage,
        bitmapImage: Bitmap,
    ): String {
        tess.setImage(bitmapImage)
        return tess.utF8Text
    }

    override suspend fun close() {
        tess.recycle()
    }
}
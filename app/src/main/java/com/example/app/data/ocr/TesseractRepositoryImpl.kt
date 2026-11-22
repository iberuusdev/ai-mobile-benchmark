package com.example.app.data.ocr

import android.content.Context
import android.graphics.Bitmap
import com.example.app.domain.repository.ocr.OCREngineRepository
import com.google.mlkit.vision.common.InputImage
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File

private const val TESSERACT_PATH = "tesseract"
private const val TESS_DATA_PATH = "tessdata"
private const val SPANISH_TRAINED_DATA_PATH = "spa.traineddata"
private const val SPANISH = "spa"

class TesseractRepositoryImpl(
    context: Context,
) : OCREngineRepository {
    private val appContext = context.applicationContext
    private val tess: TessBaseAPI = TessBaseAPI()

    init {
        val dataPath = File(appContext.filesDir, TESSERACT_PATH).absolutePath
        val tessDataDir = File(dataPath, TESS_DATA_PATH)
        tessDataDir.mkdirs()

        val trainedData = File(tessDataDir, SPANISH_TRAINED_DATA_PATH)
        if (!trainedData.exists()) {
            context.assets.open("$TESS_DATA_PATH/$SPANISH_TRAINED_DATA_PATH")
                .use { input ->
                    trainedData.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
        }
        if (!tess.init(dataPath, SPANISH)) {
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
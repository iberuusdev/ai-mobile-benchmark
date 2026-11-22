package com.example.app.data.ocr

import android.content.Context
import com.example.app.data.utils.calculateLevenshteinAccuracy
import com.example.app.domain.models.ocr.OCRBenchmarkResult
import com.example.app.domain.models.ocr.OCREngine
import com.example.app.domain.models.ocr.OCRGroundTruth
import com.example.app.domain.models.post_ocr.PostOCRGroundTruth
import com.example.app.domain.repository.ocr.OCREvaluatorRepository
import java.io.File
import kotlin.collections.iterator
import kotlin.system.measureTimeMillis

private const val ML_KIT_EXTRACTED_TEXTS = "mlkit_extracted_texts.txt"

class OCREvaluatorRepositoryImpl(
    context: Context,
) : OCREvaluatorRepository {
    private val appContext = context

    override suspend fun calculateMetrics(
        cases: Map<String, OCRGroundTruth>,
    ) : Map<OCREngine, Map<String, OCRBenchmarkResult>> {
        val engines = mapOf(
            OCREngine.ML_KIT to { MLKitOCRRepositoryImpl() },
            OCREngine.TESSERACT to { TesseractRepositoryImpl(appContext) },
        )
        val results = mutableMapOf<OCREngine, Map<String, OCRBenchmarkResult>>()
        val mlKitResults = mutableListOf<PostOCRGroundTruth>()

        for ((engine, createRepository) in engines) {
            val ocrRepository = createRepository()

            try {
                val engineResults = mutableMapOf<String, OCRBenchmarkResult>()

                for ((name, groundTruth) in cases) {
                    val image = groundTruth.image
                    val bitmap = groundTruth.bitmap
                    val expectedText = groundTruth.expectedText
                    lateinit var extractedText: String

                    val latencyMs = measureTimeMillis {
                        extractedText = ocrRepository.recognizeText(
                            image,
                            bitmap,
                        )
                    }
                    extractedText = extractedText.replace("\n", " ")

                    val result = Pair(expectedText, extractedText)
                    val accuracy = calculateLevenshteinAccuracy(
                        expectedText,
                        extractedText,
                    )

                    engineResults[name] = OCRBenchmarkResult(
                        result = result,
                        latencyMs = latencyMs,
                        accuracy = accuracy,
                    )

                    if (engine == OCREngine.ML_KIT) {
                        mlKitResults.add(
                            PostOCRGroundTruth(
                                ocrText = extractedText,
                                ocrAccuracy = accuracy,
                                expectedText = expectedText,
                            )
                        )
                    }
                }
                results[engine] = engineResults
            } finally {
                ocrRepository.close()
            }
        }
        createExtractedTextsFile(mlKitResults)
        return results
    }

    private fun createExtractedTextsFile(mlKitResults: List<PostOCRGroundTruth>) {
        val content = mlKitResults
            .joinToString(separator = "\n") { (ocrText, ocrAccuracy, expectedText) ->
                "$ocrText | $ocrAccuracy | $expectedText"
            }

        File(appContext.filesDir, ML_KIT_EXTRACTED_TEXTS)
            .writeText(content)
    }
}
package com.example.app.data.tools.ocr

import android.content.Context
import com.example.app.domain.models.OCRBenchmarkResult
import com.example.app.domain.models.OCREngine
import com.example.app.domain.models.OCRGroundTruth
import com.example.app.domain.repository.ocr.OCREvaluatorRepository
import java.io.File
import kotlin.math.max
import kotlin.system.measureTimeMillis

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
        val mlKitResults = mutableListOf<Pair<String, Float>>()

        for ((engine, createRepository) in engines) {
            val ocrRepository = createRepository()

            try {
                val engineResults = mutableMapOf<String, OCRBenchmarkResult>()

                for ((name, grounTruth) in cases) {
                    val image = grounTruth.image
                    val bitmap = grounTruth.bitmap
                    val expectedText = grounTruth.expectedText
                    lateinit var extractedText: String

                    val latencyMs = measureTimeMillis {
                        extractedText = ocrRepository.recognizeText(
                            image,
                            bitmap,
                        )
                    }
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
                            Pair(
                                extractedText.replace("\n", ""),
                                accuracy,
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

    private fun calculateLevenshteinAccuracy(
        expectedText: String,
        extractedText: String,
    ): Float {
        val expectedTextCleaned = expectedText
            .lowercase()
            .trim()
            .replace("\\s+".toRegex(), " ")
        val extractedTextCleaned = extractedText
            .lowercase()
            .trim()
            .replace("\\s+".toRegex(), " ")

        val distance = levenshteinDistance(
            expectedTextCleaned,
            extractedTextCleaned,
        )
        val maxLength = max(
            expectedTextCleaned.length,
            extractedTextCleaned.length,
        )
        if (maxLength == 0) return 100f

        val accuracy = (1f - (distance.toFloat() / maxLength.toFloat())) * 100f
        return max(0f, accuracy)
    }

    private fun levenshteinDistance(
        str1: String, str2: String,
    ): Int {
        if (str1 == str2) return 0
        if (str1.isEmpty()) return str2.length
        if (str2.isEmpty()) return str1.length

        var prev = IntArray(str2.length + 1) { it }
        var curr = IntArray(str2.length + 1)

        for (i in 1..str1.length) {
            curr[0] = i
            for (j in 1..str2.length) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                curr[j] = minOf(
                    prev[j] + 1,
                    curr[j - 1] + 1,
                    prev[j - 1] + cost,
                )
            }
            val temp = prev
            prev = curr
            curr = temp
        }

        return prev[str2.length]
    }

    private fun createExtractedTextsFile(mlKitResults: List<Pair<String, Float>>) {
        val content = mlKitResults
            .joinToString(separator = "\n") { (text, accuracy) ->
                "$text | $accuracy"
            }

        File(appContext.filesDir, "mlkit_extracted_texts.txt")
            .writeText(content)
    }
}
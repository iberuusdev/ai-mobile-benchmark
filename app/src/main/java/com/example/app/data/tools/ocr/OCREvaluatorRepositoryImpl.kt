package com.example.app.data.tools.ocr

import android.content.Context
import com.example.app.domain.models.OCRBenchmarkResult
import com.example.app.domain.models.OCRGroundTruth
import com.example.app.domain.tools.ocr.OCREvaluatorRepository
import kotlin.math.max
import kotlin.system.measureTimeMillis

class OCREvaluatorRepositoryImpl(
    context: Context,
) : OCREvaluatorRepository {
    private val appContext = context.applicationContext

    override suspend fun calculateMetrics(
        mapOfCases: Map<String, OCRGroundTruth>,
    ) : Map<String, OCRBenchmarkResult> {
        val mapOfResults: MutableMap<String, OCRBenchmarkResult> = mutableMapOf()
        val ocrRepository = MLKitOCRRepositoryImpl(appContext)

        try {
            for ((caseName, grounTruth) in mapOfCases) {
                val caseAssetPath = grounTruth.assetPath
                val caseExpectedText = grounTruth.expectedText
                var caseExtractedText: String

                val latencyMs = measureTimeMillis {
                    caseExtractedText = ocrRepository.recognizeText(caseAssetPath)
                }
                val result = Pair(caseExpectedText, caseExtractedText)
                val accuracy = calculateLevenshteinAccuracy(
                    caseExpectedText,
                    caseExtractedText,
                )

                mapOfResults[caseName] = OCRBenchmarkResult(
                    result,
                    latencyMs,
                    accuracy,
                )
            }
        } finally {
            ocrRepository.close()
        }
        return mapOfResults
    }

    private fun calculateLevenshteinAccuracy(
        expectedText: String,
        extractedText: String,
    ): Float {
        val expectedTextCleaned = expectedText.lowercase().trim().replace("\\s+".toRegex(), " ")
        val extractedTextCleaned = extractedText.lowercase().trim().replace("\\s+".toRegex(), " ")

        val distance = levenshteinDistance(expectedTextCleaned, extractedTextCleaned)
        val maxLength = max(expectedTextCleaned.length, extractedTextCleaned.length)
        if (maxLength == 0) return 100f

        val accuracy = (1f - (distance.toFloat() / maxLength.toFloat())) * 100f
        return max(0f, accuracy)
    }

    private fun levenshteinDistance(
        str1: String,
        str2: String,
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
}
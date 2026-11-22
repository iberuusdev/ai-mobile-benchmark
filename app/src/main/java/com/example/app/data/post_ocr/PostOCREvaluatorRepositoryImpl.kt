package com.example.app.data.post_ocr

import android.content.Context
import com.example.app.data.utils.calculateLevenshteinAccuracy
import com.example.app.domain.models.post_ocr.PostOCRBenchmarkResult
import com.example.app.domain.models.post_ocr.PostOCREngine
import com.example.app.domain.models.post_ocr.PostOCRGroundTruth
import com.example.app.domain.repository.post_ocr.PostOCREvaluatorRepository
import kotlin.collections.iterator
import kotlin.system.measureTimeMillis

class PostOCREvaluatorRepositoryImpl(
    context: Context,
) : PostOCREvaluatorRepository {
    private val appContext = context

    override suspend fun calculateMetrics(
        cases: Map<String, PostOCRGroundTruth>,
    ) : Map<PostOCREngine, Map<String, PostOCRBenchmarkResult>> {
        val engines = mapOf(
            PostOCREngine.GEMMA to { GemmaRepositoryImpl(appContext) },
        )
        val results = mutableMapOf<PostOCREngine, Map<String, PostOCRBenchmarkResult>>()

        for ((engine, createRepository) in engines) {
            val postOCRRepository = createRepository()

            try {
                val engineResults = mutableMapOf<String, PostOCRBenchmarkResult>()

                for ((name, groundTruth) in cases) {
                    val ocrText = groundTruth.ocrText
                    val ocrAccuracy = groundTruth.ocrAccuracy
                    val expectedText = groundTruth.expectedText
                    lateinit var correctedText: String

                    val latencyMs = measureTimeMillis {
                        correctedText = postOCRRepository.correctText(ocrText)
                    }
                    val currentAccuracy = calculateLevenshteinAccuracy(
                        expectedText,
                        correctedText,
                    )

                    engineResults[name] = PostOCRBenchmarkResult(
                        ocrText = ocrText,
                        correctedText = correctedText,
                        expectedText = expectedText,
                        latencyMs = latencyMs,
                        ocrAccuracy = ocrAccuracy,
                        currentAccuracy = currentAccuracy,
                    )
                }
                results[engine] = engineResults
            } finally {
                postOCRRepository.close()
            }
        }
        return results
    }
}
package com.example.app.domain.repository.ocr

import com.example.app.domain.models.OCRBenchmarkResult
import com.example.app.domain.models.OCREngine
import com.example.app.domain.models.OCRGroundTruth

interface OCREvaluatorRepository {
    suspend fun calculateMetrics(
        cases: Map<String, OCRGroundTruth>,
    ) : Map<OCREngine, Map<String, OCRBenchmarkResult>>
}
package com.example.app.domain.repository.ocr

import com.example.app.domain.models.ocr.OCRBenchmarkResult
import com.example.app.domain.models.ocr.OCREngine
import com.example.app.domain.models.ocr.OCRGroundTruth

interface OCREvaluatorRepository {
    suspend fun calculateMetrics(
        cases: Map<String, OCRGroundTruth>,
    ) : Map<OCREngine, Map<String, OCRBenchmarkResult>>
}
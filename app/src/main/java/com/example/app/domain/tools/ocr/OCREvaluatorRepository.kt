package com.example.app.domain.tools.ocr

import com.example.app.domain.models.OCRBenchmarkResult
import com.example.app.domain.models.OCRGroundTruth

interface OCREvaluatorRepository {
    suspend fun calculateMetrics(
        mapOfCases: Map<String, OCRGroundTruth>,
    ) : Map<String, OCRBenchmarkResult>
}
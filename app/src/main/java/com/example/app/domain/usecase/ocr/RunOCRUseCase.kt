package com.example.app.domain.usecase.ocr

import com.example.app.domain.models.OCRBenchmarkResult
import com.example.app.domain.models.OCRGroundTruth
import com.example.app.domain.tools.ocr.OCREvaluatorRepository

class RunOCRUseCase(
    private val ocrEvaluator: OCREvaluatorRepository,
) {
    suspend operator fun invoke(
        mapOfCases: Map<String, OCRGroundTruth>,
    ) : Map<String, OCRBenchmarkResult> {
        return ocrEvaluator.calculateMetrics(mapOfCases)
    }
}
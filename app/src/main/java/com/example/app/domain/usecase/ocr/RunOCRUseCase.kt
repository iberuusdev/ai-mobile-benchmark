package com.example.app.domain.usecase.ocr

import com.example.app.domain.models.ocr.OCRBenchmarkResult
import com.example.app.domain.models.ocr.OCREngine
import com.example.app.domain.models.ocr.OCRGroundTruth
import com.example.app.domain.repository.ocr.OCREvaluatorRepository

class RunOCRUseCase(
    private val ocrEvaluator: OCREvaluatorRepository,
) {
    suspend operator fun invoke(
        cases: Map<String, OCRGroundTruth>,
    ) : Map<OCREngine, Map<String, OCRBenchmarkResult>> {
        return ocrEvaluator.calculateMetrics(cases)
    }
}
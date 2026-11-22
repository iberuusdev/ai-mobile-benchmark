package com.example.app.domain.usecase.post_ocr

import com.example.app.domain.models.post_ocr.PostOCRBenchmarkResult
import com.example.app.domain.models.post_ocr.PostOCREngine
import com.example.app.domain.models.post_ocr.PostOCRGroundTruth
import com.example.app.domain.repository.post_ocr.PostOCREvaluatorRepository

class RunPostOCRUseCase(
    private val postOCREvaluator: PostOCREvaluatorRepository,
) {
    suspend operator fun invoke(
        cases: Map<String, PostOCRGroundTruth>,
    ) : Map<PostOCREngine, Map<String, PostOCRBenchmarkResult>> {
        return postOCREvaluator.calculateMetrics(cases)
    }
}
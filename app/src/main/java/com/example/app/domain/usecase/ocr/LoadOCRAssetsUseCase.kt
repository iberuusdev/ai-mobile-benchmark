package com.example.app.domain.usecase.ocr

import com.example.app.domain.models.OCRGroundTruth
import com.example.app.domain.repository.ocr.OCRAssetLoaderRepository

class LoadOCRAssetsUseCase(
    private val ocrAssetLoader: OCRAssetLoaderRepository,
) {
    suspend operator fun invoke(
        folderId: Int,
    ) : Map<String, OCRGroundTruth> {
        return ocrAssetLoader.loadAssets(folderId)
    }
}
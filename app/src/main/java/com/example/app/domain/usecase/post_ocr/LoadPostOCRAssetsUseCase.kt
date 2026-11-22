package com.example.app.domain.usecase.post_ocr

import com.example.app.domain.models.post_ocr.PostOCRGroundTruth
import com.example.app.domain.repository.post_ocr.PostOCRAssetLoaderRepository

class LoadPostOCRAssetsUseCase(
    private val postOCRAssetLoader: PostOCRAssetLoaderRepository,
) {
    suspend operator fun invoke(
        folderId: Int,
    ) : Map<String, PostOCRGroundTruth> {
        return postOCRAssetLoader.loadAssets(folderId)
    }
}
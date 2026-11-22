package com.example.app.domain.repository.post_ocr

import com.example.app.domain.models.post_ocr.PostOCRGroundTruth

interface PostOCRAssetLoaderRepository {
    suspend fun loadAssets(folderId: Int): Map<String, PostOCRGroundTruth>
}
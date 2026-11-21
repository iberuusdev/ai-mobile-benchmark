package com.example.app.domain.repository.ocr

import com.example.app.domain.models.OCRGroundTruth

interface OCRAssetLoaderRepository {
    suspend fun loadAssets(folderId: Int): Map<String, OCRGroundTruth>
}
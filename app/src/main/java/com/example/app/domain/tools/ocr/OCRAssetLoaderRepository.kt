package com.example.app.domain.tools.ocr

import com.example.app.domain.models.OCRGroundTruth

interface OCRAssetLoaderRepository {
    suspend fun loadAssets(folderId: Int): Map<String, OCRGroundTruth>
}
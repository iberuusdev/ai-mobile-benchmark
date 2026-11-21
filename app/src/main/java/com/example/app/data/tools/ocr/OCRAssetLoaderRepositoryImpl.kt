package com.example.app.data.tools.ocr

import android.content.Context
import com.example.app.domain.models.OCRGroundTruth
import com.example.app.domain.tools.ocr.OCRAssetLoaderRepository

class OCRAssetLoaderRepositoryImpl(
    context: Context,
) : OCRAssetLoaderRepository {
    private val appContext = context.applicationContext

    override suspend fun loadAssets(folderId: Int): Map<String, OCRGroundTruth> {
        val mapOfAssets: MutableMap<String, OCRGroundTruth> = mutableMapOf()

        val folderName = appContext.getString(folderId)
        val imageNames = appContext.assets
            .list(folderName)
            .orEmpty()
            .filter { fileName ->
                fileName.endsWith(".jpg", ignoreCase = true) ||
                    fileName.endsWith(".jpeg", ignoreCase = true) ||
                    fileName.endsWith(".png", ignoreCase = true)
            }
            .sorted()

        val groundTruthArrayName = "${folderName}_groundtruth"
        val groundTruthArrayId = appContext.resources.getIdentifier(
            groundTruthArrayName,
            "array",
            appContext.packageName,
        )
        if (groundTruthArrayId == 0) {
            throw IllegalArgumentException(
                "Ground truth array '$groundTruthArrayName' not found.",
            )
        }
        val groundTruthArray = appContext.resources.getStringArray(groundTruthArrayId)

        if (imageNames.size != groundTruthArray.size) {
            throw IllegalStateException(
                "Found ${imageNames.size} images but " +
                        "${groundTruthArray.size} ground-truth strings.",
            )
        }

        for ((index, imageName) in imageNames.withIndex()) {
            val baseName = imageName.substringBeforeLast('.')
            val assetPath = "$folderName/$imageName"
            val groundTruth = groundTruthArray.getOrNull(index)
                ?: throw IllegalStateException("Missing ground truth for $imageName.")

            mapOfAssets[baseName] = OCRGroundTruth(
                assetPath,
                groundTruth,
            )
        }
        return mapOfAssets.toSortedMap()
    }
}
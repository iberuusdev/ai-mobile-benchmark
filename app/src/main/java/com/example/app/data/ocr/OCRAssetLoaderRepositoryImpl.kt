package com.example.app.data.ocr

import android.content.Context
import android.graphics.BitmapFactory
import com.example.app.domain.models.ocr.OCRGroundTruth
import com.example.app.domain.repository.ocr.OCRAssetLoaderRepository
import com.google.mlkit.vision.common.InputImage

class OCRAssetLoaderRepositoryImpl(
    context: Context,
) : OCRAssetLoaderRepository {
    private val appContext = context.applicationContext

    override suspend fun loadAssets(folderId: Int): Map<String, OCRGroundTruth> {
        val mapOfAssets = mutableMapOf<String, OCRGroundTruth>()

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
            val bitmap = appContext.assets.open(assetPath).use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: throw IllegalArgumentException("Couldn't open image: $assetPath")
            val image = InputImage.fromBitmap(bitmap, 0)
            val expectedText = groundTruthArray.getOrNull(index)
                ?: throw IllegalStateException("Missing ground truth for $imageName.")

            mapOfAssets[baseName] = OCRGroundTruth(
                image = image,
                bitmap = bitmap,
                expectedText = expectedText,
            )
        }
        return mapOfAssets.toSortedMap()
    }
}
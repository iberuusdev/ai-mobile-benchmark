package com.example.app.data.post_ocr

import android.content.Context
import com.example.app.domain.models.post_ocr.PostOCRGroundTruth
import com.example.app.domain.repository.post_ocr.PostOCRAssetLoaderRepository

class PostOCRAssetLoaderRepositoryImpl(
    context: Context,
) : PostOCRAssetLoaderRepository {
    private val appContext = context.applicationContext

    override suspend fun loadAssets(folderId: Int): Map<String, PostOCRGroundTruth> {
        val mapOfAssets = mutableMapOf<String, PostOCRGroundTruth>()

        val fileName = appContext.getString(folderId)
        appContext.assets.open(fileName).bufferedReader().useLines { lines ->
            lines.forEachIndexed { index, line ->
                if (line.isNotBlank()) {
                    val segments = line.split("|")

                    if (segments.size == 3) {
                        val name = "texto%02d".format(index + 1)
                        val ocrText = segments[0].trim()
                        val ocrAccuracy = segments[1]
                            .trim()
                            .replace(",", ".")
                            .toFloatOrNull()
                                ?: 0.0f
                        val expectedText = segments[2].trim()

                        mapOfAssets[name] = PostOCRGroundTruth(
                            ocrText = ocrText,
                            ocrAccuracy = ocrAccuracy,
                            expectedText = expectedText,
                        )
                    }
                }
            }
        }
        return mapOfAssets.toSortedMap()
    }
}
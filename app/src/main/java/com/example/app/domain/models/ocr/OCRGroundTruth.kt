package com.example.app.domain.models.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage

data class OCRGroundTruth(
    val image: InputImage,
    val bitmap: Bitmap,
    val expectedText: String,
)

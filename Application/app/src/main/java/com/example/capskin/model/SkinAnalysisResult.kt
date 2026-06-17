package com.example.capskin.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SkinAnalysisResult(
    val melaninLevel: Float,
    val hemoglobinLevel: Float,
    val itaValue: Float,
    val skinType: String,
    val analysisReport: String,
    val melaninImageBase64: String? = null,
    val hemoglobinImageBase64: String? = null,
    val originalImageBase64: String? = null // 실제 촬영된 사진
) : Parcelable

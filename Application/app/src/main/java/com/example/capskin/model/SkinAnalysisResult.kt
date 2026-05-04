package com.example.capskin.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SkinAnalysisResult(
    val melaninLevel: Float,       // 멜라닌 농도
    val hemoglobinLevel: Float,    // 헤모글로빈 농도
    val itaValue: Float,           // ITA (Individual Typology Angle) - 피부 타입
    val skinType: String,          // 건성, 지성, 민감성 등
    val analysisReport: String,     // AI 가이드 텍스트
) : Parcelable

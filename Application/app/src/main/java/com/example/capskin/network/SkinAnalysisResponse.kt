package com.example.capskin.network

import com.google.gson.annotations.SerializedName

data class SkinAnalysisResponse(
    @SerializedName("melanin_level") val melaninLevel: Float?,
    @SerializedName("hemoglobin_level") val hemoglobinLevel: Float?,
    @SerializedName("ita_value") val itaValue: Float?,
    @SerializedName("skin_type") val skinType: String?,
    @SerializedName("cards") val cards: List<CardResponse>?,
    @SerializedName("melanin_map") val melaninMap: String? = null,
    @SerializedName("hemoglobin_map") val hemoglobinMap: String? = null
)

data class CardResponse(
    @SerializedName("zone") val zone: String,      // 부위 (예: cheek_left)
    @SerializedName("concern") val concern: String, // 고민 (예: 멜라닌 경향)
    @SerializedName("care") val care: String       // 관리법 (구체적인 문장)
)

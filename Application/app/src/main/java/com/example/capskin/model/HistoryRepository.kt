package com.example.capskin.model

import androidx.compose.runtime.mutableStateListOf

object HistoryRepository {
    private val _history = mutableStateListOf<SkinAnalysisResult>()
    val history: List<SkinAnalysisResult> get() = _history

    fun addResult(result: SkinAnalysisResult) {
        _history.add(0, result)
    }
}

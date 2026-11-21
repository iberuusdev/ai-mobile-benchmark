package com.example.app.features.ocr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.R
import com.example.app.domain.models.OCRBenchmarkResult
import com.example.app.domain.models.OCRGroundTruth
import com.example.app.domain.usecase.ocr.LoadOCRAssetsUseCase
import com.example.app.domain.usecase.ocr.RunOCRUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class OCRBenchmarkUiState(
    val isLoadingAssets: Boolean = false,
    val isRunningBenchmark: Boolean = false,
    val error: String? = null,
    val assets: Map<String, OCRGroundTruth> = emptyMap(),
    val results: Map<String, OCRBenchmarkResult> = emptyMap(),
) {
    val averageLatencyMs: Long?
        get() = results.values
            .map { it.latencyMs }
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?.toLong()

    val averageAccuracy: Float?
        get() = results.values
            .map { it.accuracy }
            .takeIf { it.isNotEmpty() }
            ?.average()
            ?.toFloat()
}

class OCRBenchmarkViewModel(
    private val runOCRUseCase: RunOCRUseCase,
    private val loadOCRAssetsUseCase: LoadOCRAssetsUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(OCRBenchmarkUiState())
    val state = _state.asStateFlow()

    fun loadOCRAssets() {
        val currentState = _state.value
        if (currentState.isLoadingAssets) return

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoadingAssets = true,
                assets = emptyMap(),
                error = null,
            )

            try {
                val assets = loadOCRAssetsUseCase(R.string.ocr_images)

                _state.value = _state.value.copy(
                    isLoadingAssets = false,
                    assets = assets,
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _state.value = _state.value.copy(
                    isLoadingAssets = false,
                    error = exception.message
                        ?: "Couldn't load OCR assets.",
                )
            }
        }
    }

    fun runOCRBenchmark() {
        val currentState = _state.value
        if (currentState.isRunningBenchmark) return

        val assets = _state.value.assets
        if (_state.value.isRunningBenchmark) return

        if (assets.isEmpty()) {
            _state.value = _state.value.copy(
                error = "Couldn't find OCR assets.",
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isRunningBenchmark = true,
                results = emptyMap(),
                error = null,
            )

            try {
                val results = runOCRUseCase(assets)

                _state.value = _state.value.copy(
                    isRunningBenchmark = false,
                    results = results,
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _state.value = _state.value.copy(
                    isRunningBenchmark = false,
                    error = exception.message
                        ?: "Couldn't run OCR benchmark.",
                )
            }
        }
    }
}
package com.example.app.features.post_ocr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.R
import com.example.app.domain.models.post_ocr.PostOCRBenchmarkResult
import com.example.app.domain.models.post_ocr.PostOCREngine
import com.example.app.domain.models.post_ocr.PostOCRGroundTruth
import com.example.app.domain.usecase.post_ocr.LoadPostOCRAssetsUseCase
import com.example.app.domain.usecase.post_ocr.RunPostOCRUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class PostOCRBenchmarkUiState(
    val isLoadingAssets: Boolean = false,
    val isRunningBenchmark: Boolean = false,
    val error: String? = null,
    val assets: Map<String, PostOCRGroundTruth> = emptyMap(),
    val results: Map<PostOCREngine, Map<String, PostOCRBenchmarkResult>> = emptyMap(),
) {
    fun averageLatencyMs(engine: PostOCREngine): Long? =
        results[engine]?.values
            ?.map { it.latencyMs }
            ?.takeIf { it.isNotEmpty() }
            ?.average()
            ?.toLong()

    fun averageOCRAccuracy(engine: PostOCREngine): Float? =
        results[engine]?.values
            ?.map { it.ocrAccuracy }
            ?.takeIf { it.isNotEmpty() }
            ?.average()
            ?.toFloat()

    fun averageCurrentAccuracy(engine: PostOCREngine): Float? =
        results[engine]?.values
            ?.map { it.currentAccuracy }
            ?.takeIf { it.isNotEmpty() }
            ?.average()
            ?.toFloat()
}

class PostOCRBenchmarkViewModel(
    private val runPostOCRUseCase: RunPostOCRUseCase,
    private val loadPostOCRAssetsUseCase: LoadPostOCRAssetsUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(PostOCRBenchmarkUiState())
    val state = _state.asStateFlow()

    fun loadPostOCRAssets() {
        val currentState = _state.value
        if (currentState.isLoadingAssets) return

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoadingAssets = true,
                assets = emptyMap(),
                error = null,
            )

            try {
                val assets = loadPostOCRAssetsUseCase(R.string.post_ocr_groundtruth)

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
                        ?: "Couldn't load PostOCR assets.",
                )
            }
        }
    }

    fun runPostOCRBenchmark() {
        val currentState = _state.value
        if (currentState.isRunningBenchmark) return

        val assets = _state.value.assets
        if (_state.value.isRunningBenchmark) return

        if (assets.isEmpty()) {
            _state.value = _state.value.copy(
                error = "Couldn't find PostOCR assets.",
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
                val results = runPostOCRUseCase(assets)

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
                        ?: "Couldn't run PostOCR benchmark.",
                )
            }
        }
    }
}
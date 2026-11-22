package com.example.app.features.ocr

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app.domain.models.ocr.OCREngine
import com.example.app.features.ocr.components.OCRCaseResultCard
import com.example.app.features.ocr.components.OCREngineSummaryCard

@Composable
fun OCRBenchmarkScreen(
    viewModel: OCRBenchmarkViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadOCRAssets()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = viewModel::runOCRBenchmark,
            enabled = !state.isRunningBenchmark && state.assets.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (state.isRunningBenchmark) {
                    "Ejecutando benchmark..."
                } else {
                    "Ejecutar benchmark"
                },
            )
        }

        if (state.isRunningBenchmark) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }

        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
            )
        }

        val engines = OCREngine.entries
        val columnWidth = 320.dp
        val spacing = 12.dp
        val cases = state.results.values
            .flatMap { it.keys }
            .distinct()
            .sorted()

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            Column(
                modifier = Modifier.width(
                    columnWidth * engines.size + spacing * (engines.size - 1),
                ),
                verticalArrangement = Arrangement.spacedBy(spacing),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                    engines.forEach { engine ->
                        OCREngineSummaryCard(
                            engine = engine,
                            averageLatencyMs = state.averageLatencyMs(engine),
                            averageAccuracy = state.averageAccuracy(engine),
                            modifier = Modifier.width(columnWidth),
                        )
                    }
                }

                cases.forEach { caseName ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(spacing),
                    ) {
                        engines.forEach { engine ->
                            val result = state.results[engine]?.get(caseName)

                            if (result != null) {
                                OCRCaseResultCard(
                                    caseName = caseName,
                                    result = result,
                                    modifier = Modifier
                                        .width(columnWidth)
                                        .fillMaxHeight(),
                                )
                            } else {
                                Card(
                                    modifier = Modifier
                                        .width(columnWidth)
                                        .fillMaxHeight(),
                                ) {
                                    Text(
                                        text = "Sin resultado para $caseName",
                                        modifier = Modifier.padding(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
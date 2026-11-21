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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.app.domain.models.OCRBenchmarkResult
import com.example.app.domain.models.OCREngine
import java.util.Locale

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
                        EngineSummaryCard(
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

@Composable
private fun EngineSummaryCard(
    engine: OCREngine,
    averageLatencyMs: Long?,
    averageAccuracy: Float?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = engine.displayName,
                style = MaterialTheme.typography.titleLarge,
            )

            Text(
                text = averageLatencyMs?.let {
                    "Latencia media: $it ms"
                } ?: "Latencia media: —",
            )

            Text(
                text = averageAccuracy?.let {
                    "Precisión media: ${
                        String.format(Locale.getDefault(), "%.1f", it)
                    }%"
                } ?: "Precisión media: —",
            )

            LinearProgressIndicator(
                progress = {
                    ((averageAccuracy ?: 0f) / 100f).coerceIn(0f, 1f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            )
        }
    }
}

@Composable
private fun OCRCaseResultCard(
    caseName: String,
    result: OCRBenchmarkResult,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = caseName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                )

                Text(
                    text = String.format(
                        Locale.getDefault(),
                        "%.1f%%",
                        result.accuracy,
                    ),
                    modifier = Modifier.width(64.dp),
                    textAlign = TextAlign.End,
                )

                Text(
                    text = "${result.latencyMs} ms",
                    modifier = Modifier.width(64.dp),
                    textAlign = TextAlign.End,
                )
            }

            Text(
                text = "Texto esperado",
                style = MaterialTheme.typography.labelMedium,
            )
            Text(text = result.result.first)

            Text(
                text = "Texto extraído",
                style = MaterialTheme.typography.labelMedium,
            )
            Text(text = result.result.second)
        }
    }
}
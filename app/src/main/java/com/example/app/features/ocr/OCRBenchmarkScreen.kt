package com.example.app.features.ocr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    val modelName = "Google ML Kit"
    val averageLatencyMs = state.averageLatencyMs

    val averageAccuracy = state.averageAccuracy

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = modelName,
                    style = MaterialTheme.typography.titleLarge,
                )

                Text(
                    text = if (averageLatencyMs != null) {
                        "Latencia media: $averageLatencyMs ms"
                    } else {
                        "Latencia media: —"
                    },
                )

                Text(
                    text = if (averageAccuracy != null) {
                        "Precisión media: ${
                            String.format(
                                Locale.getDefault(),
                                "%.1f",
                                averageAccuracy,
                            )
                        }%"
                    } else {
                        "Precisión media: —"
                    },
                )

                LinearProgressIndicator(
                    progress = {
                        ((averageAccuracy ?: 0f) / 100f).coerceIn(0f, 1f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                )
            }
        }

        Button(
            onClick = viewModel::runOCRBenchmark,
            enabled = !state.isRunningBenchmark && state.assets.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (state.isRunningBenchmark) {
                    "Ejecutando..."
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

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = state.results.toList(),
                key = { (caseName, _) -> caseName },
            ) { (caseName, result) ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = caseName,
                            style = MaterialTheme.typography.titleMedium,
                        )

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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Latencia: ${result.latencyMs} ms")

                            Text(
                                "Precisión: ${
                                    String.format(
                                        Locale.getDefault(),
                                        "%.1f",
                                        result.accuracy,
                                    )
                                }%",
                            )
                        }

                        LinearProgressIndicator(
                            progress = {
                                (result.accuracy / 100f).coerceIn(0f, 1f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                        )
                    }
                }
            }
        }
    }
}
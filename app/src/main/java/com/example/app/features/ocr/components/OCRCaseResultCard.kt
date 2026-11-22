package com.example.app.features.ocr.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.app.domain.models.ocr.OCRBenchmarkResult
import java.util.Locale

@Composable
fun OCRCaseResultCard(
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
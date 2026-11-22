package com.example.app.features.post_ocr.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app.domain.models.post_ocr.PostOCREngine
import java.util.Locale

@Composable
fun PostOCREngineSummaryCard(
    engine: PostOCREngine,
    averageLatencyMs: Long?,
    averageOCRAccuracy: Float?,
    averageCurrentAccuracy: Float?,
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
                text = averageCurrentAccuracy?.let {
                    "Precisión media: ${
                        String.format(Locale.getDefault(), "%.1f", it)
                    }%"
                } ?: "Precisión media: —",
            )

            LinearProgressIndicator(
                progress = {
                    ((averageCurrentAccuracy ?: 0f) / 100f).coerceIn(0f, 1f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            )
        }
    }
}
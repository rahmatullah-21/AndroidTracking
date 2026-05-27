package com.deviceinsight.pro.presentation.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deviceinsight.pro.domain.model.SecuritySeverity
import com.deviceinsight.pro.presentation.components.ScoreRing
import com.deviceinsight.pro.presentation.theme.SeverityCritical
import com.deviceinsight.pro.presentation.theme.SeverityHigh
import com.deviceinsight.pro.presentation.theme.SeverityInfo
import com.deviceinsight.pro.presentation.theme.SeverityLow
import com.deviceinsight.pro.presentation.theme.SeverityMedium

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(viewModel: SecurityViewModel = hiltViewModel()) {
    val report by viewModel.report.collectAsStateWithLifecycle()
    val scanning by viewModel.scanning.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Security Center") },
                actions = {
                    IconButton(onClick = viewModel::rescan, enabled = !scanning) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Rescan")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ScoreRing(
                            score = report.score,
                            label = "Security",
                            ringColor = scoreColor(report.score)
                        )
                        Text(
                            if (scanning) "Scanning…"
                            else "Scanned ${report.scannedAppCount} installed apps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            items(report.findings, key = { it.id }) { finding ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(10.dp).clip(CircleShape)
                            ) {
                                androidx.compose.foundation.Canvas(Modifier.size(10.dp)) {
                                    drawCircle(severityColor(finding.severity))
                                }
                            }
                            Text(
                                finding.title,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 8.dp).weight(1f)
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text(finding.severity.name) },
                                colors = AssistChipDefaults.assistChipColors(
                                    labelColor = severityColor(finding.severity)
                                )
                            )
                        }
                        Text(
                            finding.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        Text(
                            "Recommendation: ${finding.recommendation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun scoreColor(score: Int): Color = when {
    score >= 80 -> SeverityLow
    score >= 60 -> SeverityMedium
    score >= 40 -> SeverityHigh
    else -> SeverityCritical
}

private fun severityColor(severity: SecuritySeverity): Color = when (severity) {
    SecuritySeverity.CRITICAL -> SeverityCritical
    SecuritySeverity.HIGH -> SeverityHigh
    SecuritySeverity.MEDIUM -> SeverityMedium
    SecuritySeverity.LOW -> SeverityLow
    SecuritySeverity.INFO -> SeverityInfo
}

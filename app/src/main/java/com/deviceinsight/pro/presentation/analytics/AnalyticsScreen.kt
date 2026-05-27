package com.deviceinsight.pro.presentation.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deviceinsight.pro.domain.model.NetworkSummary
import com.deviceinsight.pro.presentation.components.BarChart
import com.deviceinsight.pro.presentation.components.SectionHeader
import com.deviceinsight.pro.utils.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val speed by viewModel.speed.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    Column(Modifier.padding(16.dp)) {
                        SectionHeader("Screen time (this week)")
                        MetricLine("Total foreground", Formatters.duration(state.screenTime.totalScreenOnMs))
                        MetricLine("Average session", Formatters.duration(state.screenTime.averageSessionMs))
                        MetricLine("Idle time", Formatters.duration(state.screenTime.idleTimeMs))
                        MetricLine("Peak hour", "%02d:00".format(state.screenTime.peakHour))
                        MetricLine("Focus score", "${state.screenTime.focusScore}/100")
                    }
                }
            }

            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        SectionHeader("Usage by hour")
                        BarChart(values = state.screenTime.hourlyUsageMs.map { it.toFloat() })
                        Text(
                            "00:00 – 23:00",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        SectionHeader("Daily usage (last 7 days)")
                        BarChart(
                            values = state.dailyForeground.map { it.toFloat() },
                            labels = dayLabels(state.dailyForeground.size)
                        )
                    }
                }
            }

            item {
                val total = state.network.fold(NetworkSummary(0, 0, 0, 0)) { acc, d ->
                    NetworkSummary(
                        acc.wifiRxBytes + d.summary.wifiRxBytes,
                        acc.wifiTxBytes + d.summary.wifiTxBytes,
                        acc.mobileRxBytes + d.summary.mobileRxBytes,
                        acc.mobileTxBytes + d.summary.mobileTxBytes
                    )
                }
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        SectionHeader("Network (this week)")
                        MetricLine("Wi-Fi", Formatters.bytes(total.wifiBytes))
                        MetricLine("Mobile data", Formatters.bytes(total.mobileBytes))
                        MetricLine("Total", Formatters.bytes(total.totalBytes))
                        MetricLine(
                            "Live speed",
                            "↓ ${Formatters.bytes(speed.downloadBytesPerSec)}/s  ↑ ${Formatters.bytes(speed.uploadBytesPerSec)}/s"
                        )
                    }
                }
            }

            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        SectionHeader("Performance")
                        val p = state.performance
                        val ramFraction = if (p.totalRamBytes > 0) p.usedRamBytes.toFloat() / p.totalRamBytes else 0f
                        Text("RAM ${Formatters.bytes(p.usedRamBytes)} / ${Formatters.bytes(p.totalRamBytes)}",
                            style = MaterialTheme.typography.bodySmall)
                        LinearProgressIndicator(progress = { ramFraction }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                        val storageFraction = if (p.totalStorageBytes > 0) p.usedStorageBytes.toFloat() / p.totalStorageBytes else 0f
                        Text("Storage ${Formatters.bytes(p.usedStorageBytes)} / ${Formatters.bytes(p.totalStorageBytes)}",
                            style = MaterialTheme.typography.bodySmall)
                        LinearProgressIndicator(progress = { storageFraction }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                        MetricLine("Running processes", p.runningProcessCount.toString())
                    }
                }
            }

            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        SectionHeader("Battery")
                        val b = state.battery
                        MetricLine("Level", "${b.level}%  (${b.statusText})")
                        MetricLine("Health", b.health)
                        MetricLine("Temperature", "%.1f°C".format(b.temperatureC))
                        MetricLine("Voltage", "${b.voltageMv} mV")
                        MetricLine("Technology", b.technology)
                        MetricLine("Power source", b.plugType)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun dayLabels(size: Int): List<String> {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val today = java.time.LocalDate.now().dayOfWeek.value // 1=Mon..7=Sun
    return (0 until size).map { i ->
        val dow = ((today - (size - 1 - i) - 1) % 7 + 7) % 7
        days[dow]
    }
}

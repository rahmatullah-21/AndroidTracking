package com.deviceinsight.pro.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deviceinsight.pro.presentation.components.DonutChart
import com.deviceinsight.pro.presentation.components.PermissionBanner
import com.deviceinsight.pro.presentation.components.ScoreRing
import com.deviceinsight.pro.presentation.components.SectionHeader
import com.deviceinsight.pro.presentation.components.StatCard
import com.deviceinsight.pro.presentation.components.buildSlices
import com.deviceinsight.pro.presentation.navigation.Dest
import com.deviceinsight.pro.presentation.theme.SeverityHigh
import com.deviceinsight.pro.presentation.theme.SeverityLow
import com.deviceinsight.pro.utils.Formatters
import com.deviceinsight.pro.utils.PermissionUtils
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Insight Pro") },
                actions = {
                    IconButton(onClick = { onNavigate(Dest.SETTINGS) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!state.hasUsageAccess) {
                    item {
                        PermissionBanner(
                            title = "Usage access needed",
                            message = "Grant usage access so the dashboard can show real data.",
                            actionLabel = "Grant access",
                            onAction = { context.startActivity(PermissionUtils.usageAccessIntent()) }
                        )
                    }
                }

                val summary = state.summary
                if (summary != null) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(
                                title = "Screen time",
                                value = Formatters.duration(summary.screenTime.totalScreenOnMs),
                                icon = Icons.Filled.Schedule,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Unlocks",
                                value = summary.screenTime.unlockCount.toString(),
                                icon = Icons.Filled.LockOpen,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(
                                title = "Notifications",
                                value = summary.notificationsToday.toString(),
                                icon = Icons.Filled.Notifications,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Battery",
                                value = "${summary.battery.level}%",
                                subtitle = summary.battery.statusText,
                                icon = Icons.Filled.BatteryStd,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                SectionHeader("Scores")
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    ScoreRing(
                                        score = summary.screenTime.focusScore,
                                        label = "Focus",
                                        ringColor = SeverityLow
                                    )
                                    ScoreRing(
                                        score = summary.securityScore,
                                        label = "Security",
                                        ringColor = if (summary.securityScore >= 70) SeverityLow else SeverityHigh
                                    )
                                }
                            }
                        }
                    }

                    item {
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                SectionHeader("Daily goal")
                                val goalMs = summary.dailyGoalMinutes * 60_000L
                                val progress =
                                    if (goalMs > 0) (summary.screenTime.totalScreenOnMs.toFloat() / goalMs).coerceIn(0f, 1f) else 0f
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                )
                                Text(
                                    "${Formatters.duration(summary.screenTime.totalScreenOnMs)} of " +
                                        "${summary.dailyGoalMinutes / 60}h ${summary.dailyGoalMinutes % 60}m goal",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    if (summary.topApps.isNotEmpty()) {
                        item {
                            ElevatedCard(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp)) {
                                    SectionHeader("Top apps today")
                                    DonutChart(
                                        slices = buildSlices(
                                            summary.topApps.map {
                                                it.appName to it.totalForegroundMs.toFloat()
                                            }
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    if (state.insights.isNotEmpty()) {
                        item {
                            ElevatedCard(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    SectionHeader("Insights")
                                    state.insights.forEach { insight ->
                                        Row(verticalAlignment = Alignment.Top) {
                                            Text("•  ", fontWeight = FontWeight.Bold)
                                            Text(insight, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item { Column(Modifier.padding(bottom = 16.dp)) {} }
            }
        }
    }
}

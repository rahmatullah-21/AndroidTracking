package com.deviceinsight.pro.presentation.usage

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deviceinsight.pro.domain.model.TimeRange
import com.deviceinsight.pro.domain.model.UsageSortOrder
import com.deviceinsight.pro.presentation.components.AppIcon
import com.deviceinsight.pro.presentation.components.EmptyState
import com.deviceinsight.pro.utils.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUsageScreen(viewModel: AppUsageViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("App Usage") }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Range selector
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeRange.entries.forEach { r ->
                    FilterChip(
                        selected = state.range == r,
                        onClick = { viewModel.setRange(r) },
                        label = { Text(r.label) }
                    )
                }
            }
            // Sort selector
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UsageSortOrder.entries.forEach { order ->
                    FilterChip(
                        selected = state.sortOrder == order,
                        onClick = { viewModel.setSort(order) },
                        label = { Text(order.label) }
                    )
                }
            }

            if (state.apps.isEmpty()) {
                EmptyState(
                    if (state.hasUsageAccess) "No usage recorded yet for this range."
                    else "Grant Usage Access to see app usage."
                )
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.apps, key = { it.packageName }) { app ->
                        UsageRow(app, state.totalMs)
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageRow(
    app: com.deviceinsight.pro.domain.model.AppUsageInfo,
    totalMs: Long
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        AppIcon(app.packageName, modifier = Modifier.size(40.dp))
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(app.appName, fontWeight = FontWeight.Medium, maxLines = 1)
                Text(Formatters.duration(app.totalForegroundMs), style = MaterialTheme.typography.labelLarge)
            }
            val fraction = if (totalMs > 0) (app.totalForegroundMs.toFloat() / totalMs) else 0f
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )
            Text(
                "${app.launchCount} launches • last used ${Formatters.relative(app.lastTimeUsed)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

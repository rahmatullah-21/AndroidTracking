package com.deviceinsight.pro.presentation.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deviceinsight.pro.presentation.navigation.Dest

private data class MoreEntry(val route: String, val title: String, val subtitle: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(onNavigate: (String) -> Unit) {
    val entries = listOf(
        MoreEntry(Dest.NOTIFICATIONS, "Notifications", "Search, analytics and spam detection", Icons.Filled.Notifications),
        MoreEntry(Dest.ANALYTICS, "Analytics", "Screen time, network, battery & performance", Icons.Filled.Insights),
        MoreEntry(Dest.REPORTS, "Reports & Export", "Weekly summaries and CSV export", Icons.Filled.Assessment),
        MoreEntry(Dest.SETTINGS, "Settings", "Theme, wellbeing goals and monitoring", Icons.Filled.Settings)
    )

    Scaffold(topBar = { TopAppBar(title = { Text("More") }) }) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            entries.forEach { entry ->
                ElevatedCard(
                    Modifier.fillMaxWidth().clickable { onNavigate(entry.route) }
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(entry.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Column(Modifier.padding(start = 16.dp).weight(1f)) {
                            Text(entry.title, fontWeight = FontWeight.SemiBold)
                            Text(entry.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }
        }
    }
}

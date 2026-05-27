package com.deviceinsight.pro.presentation.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deviceinsight.pro.presentation.components.EmptyState
import com.deviceinsight.pro.presentation.components.PermissionBanner
import com.deviceinsight.pro.presentation.components.StatCard
import com.deviceinsight.pro.utils.Formatters
import com.deviceinsight.pro.utils.PermissionUtils
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            if (!state.hasAccess) {
                PermissionBanner(
                    title = "Notification access needed",
                    message = "Enable notification access to capture and analyze notifications. " +
                        "Only titles and short previews are stored on-device.",
                    actionLabel = "Open settings",
                    onAction = { context.startActivity(PermissionUtils.notificationAccessIntent()) },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                label = { Text("Search notifications") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard("Today", state.todayCount.toString(), modifier = Modifier.weight(1f))
                StatCard("Possible spam", state.spamCount.toString(), modifier = Modifier.weight(1f))
            }

            if (state.items.isEmpty()) {
                EmptyState("No notifications captured yet.")
            } else {
                LazyColumn(
                    Modifier.fillMaxSize().padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.items, key = { it.id }) { n ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(n.appName, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        Formatters.clockTime(n.postedAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (n.title.isNotBlank()) Text(n.title, style = MaterialTheme.typography.bodyMedium)
                                if (n.contentPreview.isNotBlank()) {
                                    Text(
                                        n.contentPreview,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                                AssistChip(
                                    onClick = {},
                                    label = { Text(n.category.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.deviceinsight.pro.presentation.messages

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deviceinsight.pro.presentation.components.EmptyState
import com.deviceinsight.pro.presentation.components.PermissionBanner
import com.deviceinsight.pro.presentation.components.StatCard
import com.deviceinsight.pro.utils.Formatters
import com.deviceinsight.pro.utils.PermissionUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    onBack: () -> Unit,
    viewModel: MessagesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val platform by viewModel.platform.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
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
                    message = "Message monitoring reads social/messaging notifications (sender + " +
                        "preview) the system exposes. Enable Notification Access to use it. Only use " +
                        "on devices you own or are explicitly authorized to monitor.",
                    actionLabel = "Open settings",
                    onAction = { context.startActivity(PermissionUtils.notificationAccessIntent()) },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                label = { Text("Search messages & senders") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            // Platform filter
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = platform == null,
                    onClick = { viewModel.setPlatform(null) },
                    label = { Text("All") }
                )
                state.platformCounts.forEach { (p, count) ->
                    FilterChip(
                        selected = platform == p,
                        onClick = { viewModel.setPlatform(p) },
                        label = { Text("${p.displayName} ($count)") }
                    )
                }
            }

            StatCard(
                title = "Messages captured today",
                value = state.todayCount.toString(),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            if (state.messages.isEmpty()) {
                EmptyState(
                    if (state.hasAccess) "No messages captured yet."
                    else "Enable Notification Access to capture messages."
                )
            } else {
                LazyColumn(
                    Modifier.fillMaxSize().padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.messages, key = { it.id }) { msg ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(msg.sender, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        Formatters.relative(msg.timestamp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (msg.preview.isNotBlank()) {
                                    Text(
                                        msg.preview,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 3,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                                Row(
                                    Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    AssistChip(onClick = {}, label = { Text(msg.platform.displayName) })
                                    if (msg.isGroup) {
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(msg.conversation ?: "Group") }
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
}

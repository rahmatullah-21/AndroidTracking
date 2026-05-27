package com.deviceinsight.pro.presentation.timeline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deviceinsight.pro.domain.model.DeviceEventType
import com.deviceinsight.pro.presentation.components.EmptyState
import com.deviceinsight.pro.services.MonitoringService
import com.deviceinsight.pro.utils.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(viewModel: TimelineViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.monitoringEnabled) {
        if (state.monitoringEnabled) MonitoringService.start(context)
        else MonitoringService.stop(context)
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Activity Timeline") }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            Card(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Live monitoring", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Runs a foreground service to record screen, charging, Wi-Fi, " +
                                "Bluetooth and headphone events.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.monitoringEnabled,
                        onCheckedChange = viewModel::setMonitoring
                    )
                }
            }

            if (state.events.isEmpty()) {
                EmptyState("No events yet. Turn on live monitoring to start recording.")
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(state.events, key = { it.id }) { event ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                eventIcon(event.type),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                                Text(event.label, fontWeight = FontWeight.Medium)
                                event.detail?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Text(
                                Formatters.clockTime(event.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun eventIcon(type: DeviceEventType): ImageVector = when (type) {
    DeviceEventType.SCREEN_ON, DeviceEventType.SCREEN_OFF -> Icons.Filled.ScreenLockPortrait
    DeviceEventType.DEVICE_UNLOCKED, DeviceEventType.DEVICE_LOCKED -> Icons.Filled.LockOpen
    DeviceEventType.CHARGING_CONNECTED, DeviceEventType.CHARGING_DISCONNECTED -> Icons.Filled.Power
    DeviceEventType.WIFI_ENABLED, DeviceEventType.WIFI_DISABLED -> Icons.Filled.Wifi
    DeviceEventType.BLUETOOTH_CONNECTED, DeviceEventType.BLUETOOTH_DISCONNECTED -> Icons.Filled.Bluetooth
    DeviceEventType.HEADPHONES_CONNECTED, DeviceEventType.HEADPHONES_DISCONNECTED -> Icons.Filled.Headphones
    DeviceEventType.BATTERY_LOW -> Icons.Filled.BatteryAlert
    DeviceEventType.BOOT_COMPLETED -> Icons.Filled.PowerSettingsNew
    else -> Icons.Filled.Circle
}

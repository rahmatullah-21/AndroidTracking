package com.deviceinsight.pro.presentation.onboarding

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import com.deviceinsight.pro.presentation.theme.SeverityLow
import com.deviceinsight.pro.utils.PermissionUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    var recheck by remember { mutableIntStateOf(0) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { recheck++ }

    // recheck is read so these recompute whenever we resume from a settings screen.
    val hasUsage = remember(recheck) { PermissionUtils.hasUsageAccess(context) }
    val hasNotifAccess = remember(recheck) { PermissionUtils.hasNotificationAccess(context) }

    val postNotif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null
    val postGranted = postNotif?.status?.isGranted ?: true

    // Ask for the runtime notification permission automatically on first launch.
    LaunchedEffect(Unit) {
        if (postNotif != null && !postNotif.status.isGranted) postNotif.launchPermissionRequest()
    }

    val canContinue = hasUsage && hasNotifAccess

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Welcome to Device Insight Pro", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "This app analyzes usage on this device only. Nothing leaves your phone unless you " +
                "enable optional cloud sync. Grant the access below to begin.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PermissionRow(
            title = "Usage Access (required)",
            rationale = "Lets the app read app-usage time, launch counts and screen sessions.",
            granted = hasUsage,
            actionLabel = "Open Usage Access settings",
            onAction = { context.startActivity(PermissionUtils.usageAccessIntent()) }
        )

        PermissionRow(
            title = "Notification Access (required)",
            rationale = "Enables notification analytics and social/messaging monitoring. Only " +
                "titles, sender names and short previews are stored, on-device. Use only on devices " +
                "you own or are explicitly authorized to monitor (e.g. your child's device).",
            granted = hasNotifAccess,
            actionLabel = "Open Notification Access settings",
            onAction = { context.startActivity(PermissionUtils.notificationAccessIntent()) }
        )

        PermissionRow(
            title = "Show notifications (optional)",
            rationale = "Allows the monitoring service notice and limit alerts to appear.",
            granted = postGranted,
            actionLabel = "Allow notifications",
            onAction = { postNotif?.launchPermissionRequest() }
        )

        if (!canContinue) {
            Text(
                "Grant the required access above to continue. The app stays on this screen until " +
                    "Usage Access and Notification Access are enabled.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = onContinue,
            enabled = canContinue,
            modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
        ) {
            Text(if (canContinue) "Continue to dashboard" else "Complete required permissions")
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    rationale: String,
    granted: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (granted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (granted) SeverityLow else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Text(
                rationale,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 6.dp)
            )
            if (!granted) {
                OutlinedButton(onClick = onAction) { Text(actionLabel) }
            } else {
                Text("Granted", color = SeverityLow, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

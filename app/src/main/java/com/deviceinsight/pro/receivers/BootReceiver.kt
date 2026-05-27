package com.deviceinsight.pro.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.deviceinsight.pro.di.ReceiverEntryPoint
import com.deviceinsight.pro.domain.model.DeviceEventType
import com.deviceinsight.pro.services.MonitoringService
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Re-starts monitoring after a reboot, but only if the user opted in. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val appContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(appContext, ReceiverEntryPoint::class.java)
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                entryPoint.deviceEventRepository()
                    .record(DeviceEventType.BOOT_COMPLETED, "Device booted")
                if (entryPoint.settingsRepository().observe().first().monitoringEnabled) {
                    MonitoringService.start(appContext)
                }
            } finally {
                pending.finish()
            }
        }
    }
}

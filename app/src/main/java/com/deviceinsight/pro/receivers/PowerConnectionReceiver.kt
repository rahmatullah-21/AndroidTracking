package com.deviceinsight.pro.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.deviceinsight.pro.di.ReceiverEntryPoint
import com.deviceinsight.pro.domain.model.DeviceEventType
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PowerConnectionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val (type, label) = when (intent.action) {
            Intent.ACTION_POWER_CONNECTED ->
                DeviceEventType.CHARGING_CONNECTED to "Charger connected"
            Intent.ACTION_POWER_DISCONNECTED ->
                DeviceEventType.CHARGING_DISCONNECTED to "Charger disconnected"
            else -> return
        }
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext, ReceiverEntryPoint::class.java
        )
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                entryPoint.deviceEventRepository().record(type, label)
            } finally {
                pending.finish()
            }
        }
    }
}

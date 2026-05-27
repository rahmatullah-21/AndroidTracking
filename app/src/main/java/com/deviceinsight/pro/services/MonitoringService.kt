package com.deviceinsight.pro.services

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.net.wifi.WifiManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.deviceinsight.pro.R
import com.deviceinsight.pro.domain.model.DeviceEventType
import com.deviceinsight.pro.domain.repository.DeviceEventRepository
import com.deviceinsight.pro.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that registers runtime [BroadcastReceiver]s for events the system no
 * longer delivers to manifest receivers (screen, connectivity, audio, bluetooth) and records
 * them on the device-activity timeline.
 */
@AndroidEntryPoint
class MonitoringService : Service() {

    @Inject lateinit var deviceEventRepository: DeviceEventRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val (type, label, detail) = mapEvent(intent) ?: return
            record(type, label, detail)
        }
    }

    override fun onCreate() {
        super.onCreate()
        ServiceCompat.startForeground(
            this,
            Constants.MONITORING_NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
        registerReceivers()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerReceivers() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction(Intent.ACTION_BATTERY_LOW)
        }
        ContextCompat.registerReceiver(
            this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun mapEvent(intent: Intent): Triple<DeviceEventType, String, String?>? = when (intent.action) {
        Intent.ACTION_SCREEN_ON -> Triple(DeviceEventType.SCREEN_ON, "Screen on", null)
        Intent.ACTION_SCREEN_OFF -> Triple(DeviceEventType.SCREEN_OFF, "Screen off", null)
        Intent.ACTION_USER_PRESENT -> Triple(DeviceEventType.DEVICE_UNLOCKED, "Device unlocked", null)
        Intent.ACTION_BATTERY_LOW -> Triple(DeviceEventType.BATTERY_LOW, "Battery low", null)

        WifiManager.WIFI_STATE_CHANGED_ACTION -> {
            when (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)) {
                WifiManager.WIFI_STATE_ENABLED -> Triple(DeviceEventType.WIFI_ENABLED, "Wi-Fi enabled", null)
                WifiManager.WIFI_STATE_DISABLED -> Triple(DeviceEventType.WIFI_DISABLED, "Wi-Fi disabled", null)
                else -> null
            }
        }

        BluetoothDevice.ACTION_ACL_CONNECTED ->
            Triple(DeviceEventType.BLUETOOTH_CONNECTED, "Bluetooth device connected", null)
        BluetoothDevice.ACTION_ACL_DISCONNECTED ->
            Triple(DeviceEventType.BLUETOOTH_DISCONNECTED, "Bluetooth device disconnected", null)

        Intent.ACTION_HEADSET_PLUG -> {
            when (intent.getIntExtra("state", -1)) {
                1 -> Triple(DeviceEventType.HEADPHONES_CONNECTED, "Headphones connected", null)
                0 -> Triple(DeviceEventType.HEADPHONES_DISCONNECTED, "Headphones disconnected", null)
                else -> null
            }
        }

        BluetoothAdapter.ACTION_STATE_CHANGED -> null
        else -> null
    }

    private fun record(type: DeviceEventType, label: String, detail: String?) {
        scope.launch { deviceEventRepository.record(type, label, detail) }
    }

    private fun buildNotification() =
        NotificationCompat.Builder(this, Constants.MONITORING_CHANNEL_ID)
            .setContentTitle(getString(R.string.monitoring_notification_title))
            .setContentText(getString(R.string.monitoring_notification_text))
            .setSmallIcon(R.drawable.ic_stat_monitor)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()

    override fun onDestroy() {
        runCatching { unregisterReceiver(receiver) }
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context, Intent(context, MonitoringService::class.java)
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, MonitoringService::class.java))
        }
    }
}

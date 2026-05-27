package com.deviceinsight.pro.data.source

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import com.deviceinsight.pro.domain.model.BatteryInfo
import com.deviceinsight.pro.domain.model.PerformanceSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Reads live battery and performance metrics from the system. */
@Singleton
class SystemMetricsDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    fun currentBattery(): BatteryInfo {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val pct = if (level >= 0 && scale > 0) level * 100 / scale else 0

        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val healthInt = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val tech = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "—"
        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0

        return BatteryInfo(
            level = pct,
            isCharging = charging,
            statusText = statusText(status),
            health = healthText(healthInt),
            temperatureC = tempTenths / 10f,
            voltageMv = voltage,
            technology = tech,
            plugType = plugText(plugged)
        )
    }

    fun currentPerformance(): PerformanceSnapshot {
        val mem = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(mem)
        val usedRam = (mem.totalMem - mem.availMem).coerceAtLeast(0)

        val stat = StatFs(Environment.getDataDirectory().path)
        val totalStorage = stat.blockCountLong * stat.blockSizeLong
        val availStorage = stat.availableBlocksLong * stat.blockSizeLong
        val usedStorage = (totalStorage - availStorage).coerceAtLeast(0)

        val processCount = runCatching { activityManager.runningAppProcesses?.size ?: 0 }
            .getOrDefault(0)

        return PerformanceSnapshot(
            usedRamBytes = usedRam,
            totalRamBytes = mem.totalMem,
            availableRamBytes = mem.availMem,
            lowMemory = mem.lowMemory,
            usedStorageBytes = usedStorage,
            totalStorageBytes = totalStorage,
            runningProcessCount = processCount
        )
    }

    private fun statusText(status: Int) = when (status) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
        BatteryManager.BATTERY_STATUS_FULL -> "Full"
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
        else -> "Unknown"
    }

    private fun healthText(health: Int) = when (health) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
        BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
        else -> "Unknown"
    }

    private fun plugText(plugged: Int) = when (plugged) {
        BatteryManager.BATTERY_PLUGGED_AC -> "AC"
        BatteryManager.BATTERY_PLUGGED_USB -> "USB"
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
        else -> "Unplugged"
    }
}

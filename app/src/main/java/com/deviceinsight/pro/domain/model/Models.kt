package com.deviceinsight.pro.domain.model

/** Aggregated usage for a single app over a chosen window. */
data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val totalForegroundMs: Long,
    val launchCount: Int,
    val lastTimeUsed: Long
)

/** Device-wide screen-time analytics for a window. */
data class ScreenTimeStats(
    val totalScreenOnMs: Long,
    val unlockCount: Int,
    val averageSessionMs: Long,
    val idleTimeMs: Long,
    val peakHour: Int,
    val focusScore: Int,
    /** 24 buckets (index = hour of day) of foreground time in ms. */
    val hourlyUsageMs: List<Long>
)

/** A captured notification (content preview only — see privacy policy). */
data class NotificationInfo(
    val id: Long,
    val packageName: String,
    val appName: String,
    val title: String,
    val contentPreview: String,
    val category: NotificationCategory,
    val postedAt: Long
)

/** A single device-activity timeline entry. */
data class DeviceEvent(
    val id: Long,
    val type: DeviceEventType,
    val label: String,
    val detail: String?,
    val timestamp: Long
)

/** Live battery + charging snapshot. */
data class BatteryInfo(
    val level: Int,
    val isCharging: Boolean,
    val statusText: String,
    val health: String,
    val temperatureC: Float,
    val voltageMv: Int,
    val technology: String,
    val plugType: String
)

/** Device performance snapshot. */
data class PerformanceSnapshot(
    val usedRamBytes: Long,
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val lowMemory: Boolean,
    val usedStorageBytes: Long,
    val totalStorageBytes: Long,
    val runningProcessCount: Int
)

/** Network totals over a window. */
data class NetworkSummary(
    val wifiRxBytes: Long,
    val wifiTxBytes: Long,
    val mobileRxBytes: Long,
    val mobileTxBytes: Long
) {
    val totalBytes: Long get() = wifiRxBytes + wifiTxBytes + mobileRxBytes + mobileTxBytes
    val wifiBytes: Long get() = wifiRxBytes + wifiTxBytes
    val mobileBytes: Long get() = mobileRxBytes + mobileTxBytes
}

/** A single security finding surfaced by the scanner. */
data class SecurityFinding(
    val id: String,
    val title: String,
    val description: String,
    val severity: SecuritySeverity,
    val recommendation: String,
    val packageName: String? = null
)

/** Result of a device security scan. */
data class SecurityReport(
    val score: Int,
    val scannedAppCount: Int,
    val findings: List<SecurityFinding>
)

/** A single historical battery reading. */
data class BatterySample(
    val timestamp: Long,
    val level: Int,
    val temperatureC: Float
)

/** Network totals for one calendar day. */
data class NetworkDaySummary(
    val epochDay: Long,
    val summary: NetworkSummary
)

/** Instantaneous network throughput. */
data class NetworkSpeed(
    val downloadBytesPerSec: Long,
    val uploadBytesPerSec: Long
)

/** Parental-control / wellbeing configuration. */
data class WellbeingSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val monitoringEnabled: Boolean = false,
    val biometricLockEnabled: Boolean = false,
    val dailyGoalMinutes: Int = 180,
    val focusModeEnabled: Boolean = false,
    val bedtimeStartMinutes: Int = 22 * 60,
    val bedtimeEndMinutes: Int = 7 * 60,
    val blockedPackages: Set<String> = emptySet()
)

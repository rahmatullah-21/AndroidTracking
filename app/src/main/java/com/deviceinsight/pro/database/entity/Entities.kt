package com.deviceinsight.pro.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Daily per-app usage roll-up (one row per package per day). */
@Entity(
    tableName = "app_usage",
    indices = [Index(value = ["packageName", "dateEpochDay"], unique = true)]
)
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val dateEpochDay: Long,
    val totalForegroundMs: Long,
    val launchCount: Int,
    val lastTimeUsed: Long
)

/** Captured notification metadata. */
@Entity(tableName = "notifications", indices = [Index("postedAt"), Index("packageName")])
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String,
    val content: String,
    val category: String,
    val postedAt: Long
)

/** Device-activity timeline event. */
@Entity(tableName = "device_events", indices = [Index("timestamp")])
data class DeviceEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val label: String,
    val detail: String?,
    val timestamp: Long
)

/** Periodic battery sample. */
@Entity(tableName = "battery_stats", indices = [Index("timestamp")])
data class BatteryStatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val level: Int,
    val isCharging: Boolean,
    val temperatureC: Float,
    val voltageMv: Int,
    val health: String,
    val timestamp: Long
)

/** Daily network usage roll-up. */
@Entity(
    tableName = "network_usage",
    indices = [Index(value = ["dateEpochDay"], unique = true)]
)
data class NetworkUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val wifiRxBytes: Long,
    val wifiTxBytes: Long,
    val mobileRxBytes: Long,
    val mobileTxBytes: Long,
    val timestamp: Long
)

/** A captured social/messaging notification preview. */
@Entity(
    tableName = "social_messages",
    indices = [
        Index("timestamp"),
        Index("platform"),
        Index(value = ["packageName", "sender", "timestamp"], unique = true)
    ]
)
data class SocialMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val platform: String,
    val packageName: String,
    val appName: String,
    val sender: String,
    val conversation: String?,
    val preview: String,
    val isGroup: Boolean,
    val timestamp: Long
)

/** A persisted security finding from the most recent scan. */
@Entity(tableName = "security_events", indices = [Index("timestamp")])
data class SecurityEventEntity(
    @PrimaryKey val findingId: String,
    val title: String,
    val description: String,
    val severity: String,
    val recommendation: String,
    val packageName: String?,
    val timestamp: Long
)

/** Single-row settings table (id is always 0). */
@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 0,
    val themeMode: String = "SYSTEM",
    val dynamicColor: Boolean = true,
    val monitoringEnabled: Boolean = false,
    val biometricLockEnabled: Boolean = false,
    val dailyGoalMinutes: Int = 180,
    val focusModeEnabled: Boolean = false,
    val bedtimeStartMinutes: Int = 22 * 60,
    val bedtimeEndMinutes: Int = 7 * 60,
    /** Comma-separated package names blocked during focus/bedtime. */
    val blockedPackagesCsv: String = ""
)

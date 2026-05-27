package com.deviceinsight.pro.data.mapper

import com.deviceinsight.pro.database.entity.BatteryStatEntity
import com.deviceinsight.pro.database.entity.DeviceEventEntity
import com.deviceinsight.pro.database.entity.NetworkUsageEntity
import com.deviceinsight.pro.database.entity.NotificationEntity
import com.deviceinsight.pro.database.entity.SecurityEventEntity
import com.deviceinsight.pro.database.entity.UserSettingsEntity
import com.deviceinsight.pro.domain.model.BatterySample
import com.deviceinsight.pro.domain.model.DeviceEvent
import com.deviceinsight.pro.domain.model.DeviceEventType
import com.deviceinsight.pro.domain.model.NetworkDaySummary
import com.deviceinsight.pro.domain.model.NetworkSummary
import com.deviceinsight.pro.domain.model.NotificationCategory
import com.deviceinsight.pro.domain.model.NotificationInfo
import com.deviceinsight.pro.domain.model.SecurityFinding
import com.deviceinsight.pro.domain.model.SecuritySeverity
import com.deviceinsight.pro.domain.model.ThemeMode
import com.deviceinsight.pro.domain.model.WellbeingSettings

fun NotificationEntity.toDomain() = NotificationInfo(
    id = id,
    packageName = packageName,
    appName = appName,
    title = title,
    contentPreview = content,
    category = runCatching { NotificationCategory.valueOf(category) }
        .getOrDefault(NotificationCategory.OTHER),
    postedAt = postedAt
)

fun DeviceEventEntity.toDomain() = DeviceEvent(
    id = id,
    type = runCatching { DeviceEventType.valueOf(type) }.getOrDefault(DeviceEventType.UNKNOWN),
    label = label,
    detail = detail,
    timestamp = timestamp
)

fun BatteryStatEntity.toSample() = BatterySample(
    timestamp = timestamp,
    level = level,
    temperatureC = temperatureC
)

fun NetworkUsageEntity.toDomain() = NetworkDaySummary(
    epochDay = dateEpochDay,
    summary = NetworkSummary(
        wifiRxBytes = wifiRxBytes,
        wifiTxBytes = wifiTxBytes,
        mobileRxBytes = mobileRxBytes,
        mobileTxBytes = mobileTxBytes
    )
)

fun SecurityEventEntity.toDomain() = SecurityFinding(
    id = findingId,
    title = title,
    description = description,
    severity = runCatching { SecuritySeverity.valueOf(severity) }
        .getOrDefault(SecuritySeverity.INFO),
    recommendation = recommendation,
    packageName = packageName
)

fun SecurityFinding.toEntity(timestamp: Long) = SecurityEventEntity(
    findingId = id,
    title = title,
    description = description,
    severity = severity.name,
    recommendation = recommendation,
    packageName = packageName,
    timestamp = timestamp
)

fun UserSettingsEntity.toDomain() = WellbeingSettings(
    themeMode = runCatching { ThemeMode.valueOf(themeMode) }.getOrDefault(ThemeMode.SYSTEM),
    dynamicColor = dynamicColor,
    monitoringEnabled = monitoringEnabled,
    biometricLockEnabled = biometricLockEnabled,
    dailyGoalMinutes = dailyGoalMinutes,
    focusModeEnabled = focusModeEnabled,
    bedtimeStartMinutes = bedtimeStartMinutes,
    bedtimeEndMinutes = bedtimeEndMinutes,
    blockedPackages = blockedPackagesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
)

fun WellbeingSettings.toEntity() = UserSettingsEntity(
    id = 0,
    themeMode = themeMode.name,
    dynamicColor = dynamicColor,
    monitoringEnabled = monitoringEnabled,
    biometricLockEnabled = biometricLockEnabled,
    dailyGoalMinutes = dailyGoalMinutes,
    focusModeEnabled = focusModeEnabled,
    bedtimeStartMinutes = bedtimeStartMinutes,
    bedtimeEndMinutes = bedtimeEndMinutes,
    blockedPackagesCsv = blockedPackages.joinToString(",")
)

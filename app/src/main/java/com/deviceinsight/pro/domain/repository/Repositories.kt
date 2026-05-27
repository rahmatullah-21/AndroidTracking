package com.deviceinsight.pro.domain.repository

import com.deviceinsight.pro.domain.model.AppUsageInfo
import com.deviceinsight.pro.domain.model.BatteryInfo
import com.deviceinsight.pro.domain.model.BatterySample
import com.deviceinsight.pro.domain.model.DeviceEvent
import com.deviceinsight.pro.domain.model.DeviceEventType
import com.deviceinsight.pro.domain.model.NetworkDaySummary
import com.deviceinsight.pro.domain.model.NetworkSpeed
import com.deviceinsight.pro.domain.model.NotificationInfo
import com.deviceinsight.pro.domain.model.PerformanceSnapshot
import com.deviceinsight.pro.domain.model.ScreenTimeStats
import com.deviceinsight.pro.domain.model.SecurityReport
import com.deviceinsight.pro.domain.model.WellbeingSettings
import kotlinx.coroutines.flow.Flow

interface UsageRepository {
    /** Reactive per-app usage for the last [days] days (1 = today). */
    fun observeUsage(days: Int): Flow<List<AppUsageInfo>>

    /** Device-wide screen-time analytics for the last [days] days. */
    fun observeScreenTime(days: Int): Flow<ScreenTimeStats>

    fun observeTotalForegroundToday(): Flow<Long>

    /** Per-day foreground totals (ms), oldest first, length == [days]. */
    fun observeDailyForeground(days: Int): Flow<List<Long>>

    /** Pull the latest usage data from UsageStatsManager into the local DB. */
    suspend fun syncUsage(): Result<Unit>

    /** Whether the OS-level usage-access permission is currently granted. */
    fun hasUsageAccess(): Boolean
}

interface NotificationRepository {
    fun observeRecent(): Flow<List<NotificationInfo>>
    fun observeForApp(packageName: String): Flow<List<NotificationInfo>>
    fun search(query: String): Flow<List<NotificationInfo>>
    fun observeTodayCount(): Flow<Int>
    fun observeTopApps(): Flow<List<Pair<String, Int>>>
    fun hasNotificationAccess(): Boolean
}

interface DeviceEventRepository {
    fun observeRecent(): Flow<List<DeviceEvent>>
    fun observeToday(): Flow<List<DeviceEvent>>
    suspend fun record(type: DeviceEventType, label: String, detail: String? = null)
}

interface DeviceMetricsRepository {
    /** Live battery snapshot read straight from the system. */
    fun currentBattery(): BatteryInfo
    fun currentPerformance(): PerformanceSnapshot
    fun observeBatteryHistory(days: Int): Flow<List<BatterySample>>
    suspend fun sampleBattery()
}

interface NetworkRepository {
    fun observeDailyUsage(days: Int): Flow<List<NetworkDaySummary>>
    /** Emits throughput once per second while collected. */
    fun observeRealtimeSpeed(): Flow<NetworkSpeed>
    suspend fun syncToday(): Result<Unit>
}

interface SecurityRepository {
    fun observeReport(): Flow<SecurityReport>
    suspend fun runScan(): SecurityReport
}

interface SettingsRepository {
    fun observe(): Flow<WellbeingSettings>
    suspend fun update(transform: (WellbeingSettings) -> WellbeingSettings)
}

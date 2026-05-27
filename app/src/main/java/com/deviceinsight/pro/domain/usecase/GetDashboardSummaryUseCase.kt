package com.deviceinsight.pro.domain.usecase

import com.deviceinsight.pro.domain.model.AppUsageInfo
import com.deviceinsight.pro.domain.model.BatteryInfo
import com.deviceinsight.pro.domain.model.ScreenTimeStats
import com.deviceinsight.pro.domain.repository.DeviceMetricsRepository
import com.deviceinsight.pro.domain.repository.NotificationRepository
import com.deviceinsight.pro.domain.repository.SecurityRepository
import com.deviceinsight.pro.domain.repository.SettingsRepository
import com.deviceinsight.pro.domain.repository.UsageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Snapshot powering the Dashboard screen. */
data class DashboardSummary(
    val screenTime: ScreenTimeStats,
    val dailyGoalMinutes: Int,
    val topApps: List<AppUsageInfo>,
    val notificationsToday: Int,
    val securityScore: Int,
    val battery: BatteryInfo
)

class GetDashboardSummaryUseCase @Inject constructor(
    private val usageRepository: UsageRepository,
    private val notificationRepository: NotificationRepository,
    private val securityRepository: SecurityRepository,
    private val settingsRepository: SettingsRepository,
    private val deviceMetricsRepository: DeviceMetricsRepository
) {
    operator fun invoke(): Flow<DashboardSummary> = combine(
        usageRepository.observeScreenTime(days = 1),
        usageRepository.observeUsage(days = 1),
        notificationRepository.observeTodayCount(),
        securityRepository.observeReport(),
        settingsRepository.observe()
    ) { screenTime, usage, notifCount, report, settings ->
        DashboardSummary(
            screenTime = screenTime,
            dailyGoalMinutes = settings.dailyGoalMinutes,
            topApps = usage.take(5),
            notificationsToday = notifCount,
            securityScore = report.score,
            battery = deviceMetricsRepository.currentBattery()
        )
    }
}

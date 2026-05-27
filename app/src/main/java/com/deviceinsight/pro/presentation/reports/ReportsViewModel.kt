package com.deviceinsight.pro.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviceinsight.pro.domain.model.AppUsageInfo
import com.deviceinsight.pro.domain.model.ScreenTimeStats
import com.deviceinsight.pro.domain.repository.NetworkRepository
import com.deviceinsight.pro.domain.repository.UsageRepository
import com.deviceinsight.pro.utils.Formatters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ReportUiState(
    val screenTime: ScreenTimeStats = ScreenTimeStats(0, 0, 0, 0, 0, 0, List(24) { 0L }),
    val topApps: List<AppUsageInfo> = emptyList(),
    val totalNetworkBytes: Long = 0,
    val avgDailyMs: Long = 0
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    usageRepository: UsageRepository,
    networkRepository: NetworkRepository
) : ViewModel() {

    val state: StateFlow<ReportUiState> = combine(
        usageRepository.observeScreenTime(7),
        usageRepository.observeUsage(7),
        networkRepository.observeDailyUsage(7)
    ) { screen, apps, net ->
        ReportUiState(
            screenTime = screen,
            topApps = apps.take(20),
            totalNetworkBytes = net.sumOf { it.summary.totalBytes },
            avgDailyMs = screen.totalScreenOnMs / 7
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportUiState())

    /** Builds a CSV weekly report from the current state. */
    fun buildCsv(): String {
        val s = state.value
        val sb = StringBuilder()
        sb.appendLine("Device Insight Pro — Weekly Report")
        sb.appendLine("Metric,Value")
        sb.appendLine("Total screen time (7d),${Formatters.duration(s.screenTime.totalScreenOnMs)}")
        sb.appendLine("Average per day,${Formatters.duration(s.avgDailyMs)}")
        sb.appendLine("Unlocks,${s.screenTime.unlockCount}")
        sb.appendLine("Focus score,${s.screenTime.focusScore}")
        sb.appendLine("Total network,${Formatters.bytes(s.totalNetworkBytes)}")
        sb.appendLine()
        sb.appendLine("App,Package,Foreground time,Launches")
        s.topApps.forEach {
            sb.appendLine(
                "\"${it.appName}\",${it.packageName},${Formatters.duration(it.totalForegroundMs)},${it.launchCount}"
            )
        }
        return sb.toString()
    }
}

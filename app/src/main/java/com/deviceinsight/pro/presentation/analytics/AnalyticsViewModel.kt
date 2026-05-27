package com.deviceinsight.pro.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviceinsight.pro.domain.model.BatteryInfo
import com.deviceinsight.pro.domain.model.NetworkDaySummary
import com.deviceinsight.pro.domain.model.NetworkSpeed
import com.deviceinsight.pro.domain.model.PerformanceSnapshot
import com.deviceinsight.pro.domain.model.ScreenTimeStats
import com.deviceinsight.pro.domain.repository.DeviceMetricsRepository
import com.deviceinsight.pro.domain.repository.NetworkRepository
import com.deviceinsight.pro.domain.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AnalyticsUiState(
    val screenTime: ScreenTimeStats,
    val dailyForeground: List<Long>,
    val network: List<NetworkDaySummary>,
    val performance: PerformanceSnapshot,
    val battery: BatteryInfo
) {
    companion object {
        val EMPTY = AnalyticsUiState(
            screenTime = ScreenTimeStats(0, 0, 0, 0, 0, 0, List(24) { 0L }),
            dailyForeground = List(7) { 0L },
            network = emptyList(),
            performance = PerformanceSnapshot(0, 0, 0, false, 0, 0, 0),
            battery = BatteryInfo(0, false, "—", "—", 0f, 0, "—", "—")
        )
    }
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    usageRepository: UsageRepository,
    networkRepository: NetworkRepository,
    private val deviceMetricsRepository: DeviceMetricsRepository
) : ViewModel() {

    val state: StateFlow<AnalyticsUiState> = combine(
        usageRepository.observeScreenTime(7),
        usageRepository.observeDailyForeground(7),
        networkRepository.observeDailyUsage(7)
    ) { screen, daily, net ->
        AnalyticsUiState(
            screenTime = screen,
            dailyForeground = daily,
            network = net,
            performance = deviceMetricsRepository.currentPerformance(),
            battery = deviceMetricsRepository.currentBattery()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyticsUiState.EMPTY)

    val speed: StateFlow<NetworkSpeed> = networkRepository.observeRealtimeSpeed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1_000), NetworkSpeed(0, 0))
}

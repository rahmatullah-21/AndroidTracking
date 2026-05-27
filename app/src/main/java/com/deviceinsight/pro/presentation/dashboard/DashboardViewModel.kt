package com.deviceinsight.pro.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviceinsight.pro.domain.repository.DeviceMetricsRepository
import com.deviceinsight.pro.domain.repository.NetworkRepository
import com.deviceinsight.pro.domain.repository.SecurityRepository
import com.deviceinsight.pro.domain.repository.UsageRepository
import com.deviceinsight.pro.domain.usecase.DashboardSummary
import com.deviceinsight.pro.domain.usecase.GenerateInsightsUseCase
import com.deviceinsight.pro.domain.usecase.GetDashboardSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val loading: Boolean = true,
    val hasUsageAccess: Boolean = true,
    val summary: DashboardSummary? = null,
    val insights: List<String> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getDashboardSummary: GetDashboardSummaryUseCase,
    private val generateInsights: GenerateInsightsUseCase,
    private val usageRepository: UsageRepository,
    private val networkRepository: NetworkRepository,
    private val securityRepository: SecurityRepository,
    private val deviceMetricsRepository: DeviceMetricsRepository
) : ViewModel() {

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    val state: StateFlow<DashboardUiState> = getDashboardSummary()
        .map { summary ->
            DashboardUiState(
                loading = false,
                hasUsageAccess = usageRepository.hasUsageAccess(),
                summary = summary,
                insights = generateInsights(
                    summary.screenTime,
                    summary.topApps,
                    summary.dailyGoalMinutes
                )
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            runCatching {
                usageRepository.syncUsage()
                networkRepository.syncToday()
                deviceMetricsRepository.sampleBattery()
                securityRepository.runScan()
            }
            _refreshing.value = false
        }
    }
}

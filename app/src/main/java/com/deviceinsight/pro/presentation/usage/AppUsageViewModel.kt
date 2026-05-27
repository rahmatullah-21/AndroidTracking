package com.deviceinsight.pro.presentation.usage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviceinsight.pro.domain.model.AppUsageInfo
import com.deviceinsight.pro.domain.model.TimeRange
import com.deviceinsight.pro.domain.model.UsageSortOrder
import com.deviceinsight.pro.domain.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppUsageUiState(
    val apps: List<AppUsageInfo> = emptyList(),
    val sortOrder: UsageSortOrder = UsageSortOrder.MOST_USED,
    val range: TimeRange = TimeRange.TODAY,
    val hasUsageAccess: Boolean = true,
    val totalMs: Long = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AppUsageViewModel @Inject constructor(
    private val usageRepository: UsageRepository
) : ViewModel() {

    private val sortOrder = MutableStateFlow(UsageSortOrder.MOST_USED)
    private val range = MutableStateFlow(TimeRange.TODAY)

    val state: StateFlow<AppUsageUiState> =
        combine(sortOrder, range) { sort, r -> sort to r }
            .flatMapLatest { (sort, r) ->
                usageRepository.observeUsage(r.days).map { apps ->
                    AppUsageUiState(
                        apps = sortApps(apps, sort),
                        sortOrder = sort,
                        range = r,
                        hasUsageAccess = usageRepository.hasUsageAccess(),
                        totalMs = apps.sumOf { it.totalForegroundMs }
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppUsageUiState())

    init {
        viewModelScope.launch { usageRepository.syncUsage() }
    }

    fun setSort(order: UsageSortOrder) { sortOrder.value = order }
    fun setRange(r: TimeRange) { range.value = r }

    private fun sortApps(apps: List<AppUsageInfo>, order: UsageSortOrder) = when (order) {
        UsageSortOrder.MOST_USED -> apps.sortedByDescending { it.totalForegroundMs }
        UsageSortOrder.LEAST_USED -> apps.sortedBy { it.totalForegroundMs }
        UsageSortOrder.RECENTLY_OPENED -> apps.sortedByDescending { it.lastTimeUsed }
        UsageSortOrder.MOST_LAUNCHED -> apps.sortedByDescending { it.launchCount }
    }
}

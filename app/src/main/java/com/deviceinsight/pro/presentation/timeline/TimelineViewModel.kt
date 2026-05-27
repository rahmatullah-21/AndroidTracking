package com.deviceinsight.pro.presentation.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviceinsight.pro.domain.model.DeviceEvent
import com.deviceinsight.pro.domain.repository.DeviceEventRepository
import com.deviceinsight.pro.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimelineUiState(
    val events: List<DeviceEvent> = emptyList(),
    val monitoringEnabled: Boolean = false
)

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val deviceEventRepository: DeviceEventRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val state: StateFlow<TimelineUiState> = combine(
        deviceEventRepository.observeRecent(),
        settingsRepository.observe()
    ) { events, settings ->
        TimelineUiState(events = events, monitoringEnabled = settings.monitoringEnabled)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimelineUiState())

    fun setMonitoring(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.update { it.copy(monitoringEnabled = enabled) }
        }
    }
}

package com.deviceinsight.pro.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviceinsight.pro.domain.model.ThemeMode
import com.deviceinsight.pro.domain.model.WellbeingSettings
import com.deviceinsight.pro.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<WellbeingSettings> = settingsRepository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WellbeingSettings())

    private fun mutate(transform: (WellbeingSettings) -> WellbeingSettings) {
        viewModelScope.launch { settingsRepository.update(transform) }
    }

    fun setThemeMode(mode: ThemeMode) = mutate { it.copy(themeMode = mode) }
    fun setDynamicColor(enabled: Boolean) = mutate { it.copy(dynamicColor = enabled) }
    fun setDailyGoal(minutes: Int) = mutate { it.copy(dailyGoalMinutes = minutes) }
    fun setFocusMode(enabled: Boolean) = mutate { it.copy(focusModeEnabled = enabled) }
    fun setMonitoring(enabled: Boolean) = mutate { it.copy(monitoringEnabled = enabled) }
    fun setBiometricLock(enabled: Boolean) = mutate { it.copy(biometricLockEnabled = enabled) }
}

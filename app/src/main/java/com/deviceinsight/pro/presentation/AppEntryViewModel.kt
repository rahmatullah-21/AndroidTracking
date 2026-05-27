package com.deviceinsight.pro.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviceinsight.pro.domain.model.WellbeingSettings
import com.deviceinsight.pro.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Supplies theme settings to the top-level [com.deviceinsight.pro.presentation.theme.DeviceInsightTheme]. */
@HiltViewModel
class AppEntryViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<WellbeingSettings> = settingsRepository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WellbeingSettings())
}

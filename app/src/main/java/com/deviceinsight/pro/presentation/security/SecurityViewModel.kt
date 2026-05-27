package com.deviceinsight.pro.presentation.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviceinsight.pro.domain.model.SecurityReport
import com.deviceinsight.pro.domain.repository.SecurityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val securityRepository: SecurityRepository
) : ViewModel() {

    val report: StateFlow<SecurityReport> = securityRepository.observeReport()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            SecurityReport(score = 100, scannedAppCount = 0, findings = emptyList())
        )

    private val _scanning = MutableStateFlow(false)
    val scanning: StateFlow<Boolean> = _scanning.asStateFlow()

    init { rescan() }

    fun rescan() {
        viewModelScope.launch {
            _scanning.value = true
            runCatching { securityRepository.runScan() }
            _scanning.value = false
        }
    }
}

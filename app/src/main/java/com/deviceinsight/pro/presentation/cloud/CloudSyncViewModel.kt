package com.deviceinsight.pro.presentation.cloud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviceinsight.pro.data.repository.DeviceLinkRepository
import com.deviceinsight.pro.domain.repository.CloudSyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CloudSyncUiState(
    val available: Boolean = false,
    val linked: Boolean = false,
    val busy: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class CloudSyncViewModel @Inject constructor(
    private val deviceLink: DeviceLinkRepository,
    private val cloudSync: CloudSyncRepository
) : ViewModel() {

    private val _state = MutableStateFlow(snapshot())
    val state: StateFlow<CloudSyncUiState> = _state.asStateFlow()

    private fun snapshot(busy: Boolean = false, message: String? = null, error: String? = null) =
        CloudSyncUiState(
            available = deviceLink.isAvailable(),
            linked = deviceLink.isLinked(),
            busy = busy,
            message = message,
            error = error
        )

    fun link(code: String, consentGiven: Boolean) {
        if (!consentGiven) {
            _state.value = snapshot(error = "Please confirm you're authorized to monitor this device.")
            return
        }
        viewModelScope.launch {
            _state.value = snapshot(busy = true)
            val result = deviceLink.linkWithCode(code)
            if (result.isSuccess) {
                runCatching { cloudSync.syncNow() }
                _state.value = snapshot(message = "Device linked. It will appear in the admin panel shortly.")
            } else {
                _state.value = snapshot(error = result.exceptionOrNull()?.message ?: "Linking failed")
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _state.value = snapshot(busy = true)
            val result = cloudSync.syncNow()
            _state.value = snapshot(
                message = if (result.isSuccess) "Synced. Data should appear in the admin panel shortly." else null,
                error = result.exceptionOrNull()?.message
            )
        }
    }

    fun unlink() {
        viewModelScope.launch {
            _state.value = snapshot(busy = true)
            deviceLink.unlink()
            _state.value = snapshot(message = "Device unlinked.")
        }
    }
}

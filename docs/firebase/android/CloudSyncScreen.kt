/*
 * DROP-IN REFERENCE — not compiled by default. The device-side "Cloud sync" screen for the
 * pairing-code flow. Copy to app/src/main/java/com/deviceinsight/pro/presentation/cloud/ once
 * Firebase + DeviceLinkRepository are enabled, then add a route to it from SettingsScreen.
 *
 * UX: the device user enters the code shown in the admin panel, explicitly consents to monitoring,
 * and links. After linking, CloudSyncWorker keeps the data fresh.
 */
package com.deviceinsight.pro.presentation.cloud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviceinsight.pro.data.repository.DeviceLinkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CloudSyncUiState(
    val linked: Boolean = false,
    val busy: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CloudSyncViewModel @Inject constructor(
    private val deviceLink: DeviceLinkRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CloudSyncUiState(linked = deviceLink.isLinked))
    val state: StateFlow<CloudSyncUiState> = _state.asStateFlow()

    fun link(code: String, consentGiven: Boolean) {
        if (!consentGiven) {
            _state.value = _state.value.copy(error = "You must confirm consent to monitor this device.")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(busy = true, error = null)
            val result = deviceLink.linkWithCode(code)
            _state.value = CloudSyncUiState(
                linked = result.isSuccess,
                busy = false,
                error = result.exceptionOrNull()?.message
            )
            // On success: schedule CloudSyncWorker (e.g. WorkManager periodic, every 2h) + run once.
        }
    }

    fun unlink() {
        viewModelScope.launch {
            deviceLink.unlink()
            _state.value = CloudSyncUiState(linked = false)
        }
    }
}

/*
@Composable
fun CloudSyncScreen(viewModel: CloudSyncViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var code by remember { mutableStateOf("") }
    var consent by remember { mutableStateOf(false) }

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.linked) {
            Text("This device is linked and syncing to your admin account.")
            Button(onClick = viewModel::unlink) { Text("Unlink device") }
        } else {
            Text("Enter the pairing code from the admin panel to link this device.")
            OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Pairing code") })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = consent, onCheckedChange = { consent = it })
                Text("I confirm I own or am authorized to monitor this device.")
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = { viewModel.link(code, consent) }, enabled = !state.busy) {
                Text(if (state.busy) "Linking…" else "Link device")
            }
        }
    }
}
*/

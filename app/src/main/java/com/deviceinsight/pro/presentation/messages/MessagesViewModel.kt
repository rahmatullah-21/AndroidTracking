package com.deviceinsight.pro.presentation.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviceinsight.pro.domain.model.SocialMessage
import com.deviceinsight.pro.domain.model.SocialPlatform
import com.deviceinsight.pro.domain.repository.SocialMessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MessagesUiState(
    val messages: List<SocialMessage> = emptyList(),
    val todayCount: Int = 0,
    val platformCounts: List<Pair<SocialPlatform, Int>> = emptyList(),
    val hasAccess: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val repository: SocialMessageRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _platform = MutableStateFlow<SocialPlatform?>(null)
    val platform: StateFlow<SocialPlatform?> = _platform.asStateFlow()

    private val messages = combine(_query, _platform) { q, p -> q to p }
        .flatMapLatest { (q, p) ->
            when {
                q.isNotBlank() -> repository.search(q)
                p != null -> repository.observeForPlatform(p)
                else -> repository.observeRecent()
            }
        }

    val state: StateFlow<MessagesUiState> = combine(
        messages,
        repository.observeTodayCount(),
        repository.observePlatformCounts()
    ) { msgs, count, counts ->
        MessagesUiState(
            messages = msgs,
            todayCount = count,
            platformCounts = counts,
            hasAccess = repository.hasNotificationAccess()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MessagesUiState())

    fun setQuery(q: String) { _query.value = q }
    fun setPlatform(p: SocialPlatform?) { _platform.value = p }
}

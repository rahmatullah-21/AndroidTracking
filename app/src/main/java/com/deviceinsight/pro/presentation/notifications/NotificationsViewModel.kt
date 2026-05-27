package com.deviceinsight.pro.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deviceinsight.pro.domain.model.NotificationCategory
import com.deviceinsight.pro.domain.model.NotificationInfo
import com.deviceinsight.pro.domain.repository.NotificationRepository
import com.deviceinsight.pro.services.NotificationCategorizer
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

data class NotificationsUiState(
    val items: List<NotificationInfo> = emptyList(),
    val todayCount: Int = 0,
    val topApps: List<Pair<String, Int>> = emptyList(),
    val spamCount: Int = 0,
    val hasAccess: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val results = _query.flatMapLatest { repository.search(it) }

    val state: StateFlow<NotificationsUiState> = combine(
        results,
        repository.observeTodayCount(),
        repository.observeTopApps()
    ) { items, count, top ->
        NotificationsUiState(
            items = items,
            todayCount = count,
            topApps = top,
            spamCount = items.count {
                it.category == NotificationCategory.PROMOTION ||
                    NotificationCategorizer.looksPromotional(it.title, it.contentPreview)
            },
            hasAccess = repository.hasNotificationAccess()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationsUiState())

    fun setQuery(q: String) { _query.value = q }
}

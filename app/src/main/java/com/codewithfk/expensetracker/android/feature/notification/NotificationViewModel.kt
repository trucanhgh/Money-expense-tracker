package com.codewithfk.expensetracker.android.feature.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codewithfk.expensetracker.android.data.model.NotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repo: com.codewithfk.expensetracker.android.data.repository.NotificationRepository
) : ViewModel() {

    val notifications: StateFlow<List<NotificationEntity>> = repo.getNotifications()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val unreadCount = repo.getUnreadCount()
        .map { it }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun markAllRead() {
        viewModelScope.launch {
            repo.markAllRead()
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repo.clearAll()
        }
    }
}

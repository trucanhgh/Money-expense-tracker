package com.codewithfk.expensetracker.android.data.repository

import com.codewithfk.expensetracker.android.data.model.NotificationEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: com.codewithfk.expensetracker.android.data.dao.NotificationDao,
    private val currentUserProvider: com.codewithfk.expensetracker.android.auth.CurrentUserProvider
) {
    private val ownerId: String = currentUserProvider.getUserId() ?: ""

    fun getNotifications(): Flow<List<NotificationEntity>> = notificationDao.getNotifications(ownerId)

    fun getUnreadCount(): Flow<Int> = notificationDao.getUnreadCount(ownerId)

    suspend fun insertNotification(notification: NotificationEntity) {
        notificationDao.insert(notification.copy(ownerId = ownerId))
    }

    suspend fun insertNotification(title: String, message: String, timestamp: Long = System.currentTimeMillis(), type: String = "") {
        val n = NotificationEntity(id = null, title = title, message = message, timestamp = timestamp, isRead = false, type = type, ownerId = ownerId)
        notificationDao.insert(n)
    }

    suspend fun markAllRead() {
        notificationDao.markAllRead(ownerId)
    }

    suspend fun clearAll() {
        notificationDao.clearAll(ownerId)
    }
}

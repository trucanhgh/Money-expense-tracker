package com.codewithfk.expensetracker.android.data.dao

import androidx.room.*
import com.codewithfk.expensetracker.android.data.model.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity): Long

    @Query("SELECT * FROM notification_table WHERE ownerId = :ownerId ORDER BY timestamp DESC")
    fun getNotifications(ownerId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notification_table WHERE ownerId = :ownerId AND isRead = 0")
    fun getUnreadCount(ownerId: String): Flow<Int>

    @Query("UPDATE notification_table SET isRead = 1 WHERE ownerId = :ownerId AND isRead = 0")
    suspend fun markAllRead(ownerId: String)

    @Query("DELETE FROM notification_table WHERE ownerId = :ownerId")
    suspend fun clearAll(ownerId: String)

    @Delete
    suspend fun delete(notification: NotificationEntity)
}


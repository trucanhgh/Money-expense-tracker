package com.codewithfk.expensetracker.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_table")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val type: String = "",
    val ownerId: String = ""
)


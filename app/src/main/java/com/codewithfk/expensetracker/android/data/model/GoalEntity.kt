package com.codewithfk.expensetracker.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goal_table")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val name: String,
    val targetAmount: Double = 0.0,
    val frequency: String? = null, // "weekly", "monthly" or null
    val reminderEnabled: Boolean = false,
    val ownerId: String = "" // owner / user id
)

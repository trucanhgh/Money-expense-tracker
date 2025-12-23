package com.codewithfk.expensetracker.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_table")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val name: String,
    val ownerId: String = "", // owner / user id

    // Auto transaction configuration
    val isAutoTransactionEnabled: Boolean = false,
    val autoAmount: Double = 0.0,
    val autoType: String = "Expense", // "Expense" | "Income"
    val autoRepeatType: String = "WEEKLY", // "WEEKLY" | "MONTHLY"
    val autoDayOfWeek: Int? = null,   // 1..7 (weekly). 1=Monday
    val autoDayOfMonth: Int? = null,  // 1..28 (monthly)
    val lastAutoExecutedDate: String? = null // stored as dd/MM/yyyy
)

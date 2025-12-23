@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.codewithfk.expensetracker.android.feature.transactionlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codewithfk.expensetracker.android.data.dao.ExpenseDao
import com.codewithfk.expensetracker.android.data.model.ExpenseEntity
import com.codewithfk.expensetracker.android.auth.CurrentUserProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val dao: ExpenseDao,
    currentUserProvider: CurrentUserProvider
) : ViewModel() {
    private val userId: String = currentUserProvider.getUserId() ?: ""

    // params are handled by collecting the chosen DAO flow into baseExpensesState
    private var baseCollectorJob: Job? = null
    private val baseExpensesState = MutableStateFlow<List<ExpenseEntity>>(emptyList())

    fun setParams(categoryName: String?, goalName: String?) {
        // cancel previous collector
        baseCollectorJob?.cancel()
        val flow = when {
            !categoryName.isNullOrBlank() -> dao.getExpensesByCategory(userId, categoryName)
            !goalName.isNullOrBlank() -> dao.getExpensesByCategory(userId, goalName)
            else -> dao.getAllExpense(userId)
        }
        baseCollectorJob = viewModelScope.launch {
            flow.collect { list ->
                baseExpensesState.value = list
            }
        }
    }

    // Filter state
    private val _filterType = MutableStateFlow("Tất cả") // "Tất cả" | "Chi tiêu" | "Thu nhập"
    private val _dateRange = MutableStateFlow(DateRange.ALL)

    // Expose filter state as read-only StateFlows for the UI
    val filterType: StateFlow<String> = _filterType.asStateFlow()
    val dateRange: StateFlow<DateRange> = _dateRange.asStateFlow()

    fun setFilterType(type: String) {
        _filterType.value = type
    }

    fun setDateRange(range: DateRange) {
        _dateRange.value = range
    }

    fun resetFilters() {
        _filterType.value = "Tất cả"
        _dateRange.value = DateRange.ALL
    }

    // Combined filtered flow
    val filteredExpenses: StateFlow<List<ExpenseEntity>> = combine(
        baseExpensesState,
        _filterType,
        _dateRange
    ) { base, type, range ->
        // Apply type filter
        val byType = when (type) {
            "Chi tiêu" -> base.filter { it.type == "Expense" }
            "Thu nhập" -> base.filter { it.type == "Income" }
            else -> base
        }

        // Apply date range filter
        val now = System.currentTimeMillis()
        val filteredByDate = when (range) {
            DateRange.ALL -> byType
            DateRange.TODAY -> byType.filter { UtilsDate.isSameDay(UtilsDate.getMillisFromDate(it.date), now) }
            DateRange.YESTERDAY -> byType.filter { UtilsDate.isSameDay(UtilsDate.getMillisFromDate(it.date), now - 24 * 60 * 60 * 1000) }
            DateRange.LAST_30_DAYS -> byType.filter { UtilsDate.getMillisFromDate(it.date) >= now - 30L * 24 * 60 * 60 * 1000 }
            DateRange.LAST_90_DAYS -> byType.filter { UtilsDate.getMillisFromDate(it.date) >= now - 90L * 24 * 60 * 60 * 1000 }
            DateRange.LAST_365_DAYS -> byType.filter { UtilsDate.getMillisFromDate(it.date) >= now - 365L * 24 * 60 * 60 * 1000 }
        }

        // Sort descending by date
        filteredByDate.sortedByDescending { UtilsDate.getMillisFromDate(it.date) }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

// Small helper for date utilities local to this ViewModel file to avoid depending on UI Utils
object UtilsDate {
    // Parses dd/MM/yyyy to millis; if parse fails returns 0
    fun getMillisFromDate(dateStr: String): Long {
        return try {
            val parts = dateStr.split('/')
            if (parts.size >= 3) {
                val day = parts[0].toInt()
                val month = parts[1].toInt()
                val year = parts[2].toInt()
                val cal = java.util.Calendar.getInstance()
                cal.set(java.util.Calendar.YEAR, year)
                cal.set(java.util.Calendar.MONTH, month - 1)
                cal.set(java.util.Calendar.DAY_OF_MONTH, day)
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                cal.timeInMillis
            } else {
                0L
            }
        } catch (_: Exception) {
            0L
        }
    }

    fun isSameDay(millisA: Long, millisB: Long): Boolean {
        if (millisA <= 0L || millisB <= 0L) return false
        val calA = java.util.Calendar.getInstance().apply { timeInMillis = millisA }
        val calB = java.util.Calendar.getInstance().apply { timeInMillis = millisB }
        return calA.get(java.util.Calendar.YEAR) == calB.get(java.util.Calendar.YEAR)
                && calA.get(java.util.Calendar.DAY_OF_YEAR) == calB.get(java.util.Calendar.DAY_OF_YEAR)
    }
}

enum class DateRange {
    ALL, TODAY, YESTERDAY, LAST_30_DAYS, LAST_90_DAYS, LAST_365_DAYS
}

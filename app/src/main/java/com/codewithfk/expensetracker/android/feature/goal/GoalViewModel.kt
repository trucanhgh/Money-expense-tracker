package com.codewithfk.expensetracker.android.feature.goal

import androidx.lifecycle.viewModelScope
import com.codewithfk.expensetracker.android.base.BaseViewModel
import com.codewithfk.expensetracker.android.base.UiEvent
import com.codewithfk.expensetracker.android.data.dao.GoalDao
import com.codewithfk.expensetracker.android.data.dao.ExpenseDao
import com.codewithfk.expensetracker.android.data.model.GoalEntity
import com.codewithfk.expensetracker.android.data.model.ExpenseEntity
import com.codewithfk.expensetracker.android.auth.CurrentUserProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import com.codewithfk.expensetracker.android.data.repository.NotificationRepository
import javax.inject.Inject

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val goalDao: GoalDao,
    private val expenseDao: ExpenseDao,
    private val currentUserProvider: CurrentUserProvider,
    private val notificationRepository: NotificationRepository
) : BaseViewModel() {

    private val userId: String = currentUserProvider.getUserId() ?: ""

    // One-time UI messages (errors, toasts, snackbars)
    private val _uiMessage = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 1)
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    val goals: Flow<List<GoalEntity>> = goalDao.getAllGoals(userId)

    fun getContributionsForGoal(name: String, month: String? = null): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByCategory(userId, name, month)
    }

    fun insertGoal(goal: GoalEntity) {
        viewModelScope.launch {
            val trimmed = goal.name.trim()
            if (trimmed.isNotEmpty()) {
                goalDao.insertGoal(GoalEntity(name = trimmed, targetAmount = goal.targetAmount, frequency = goal.frequency, reminderEnabled = goal.reminderEnabled, ownerId = userId))
                // notify
                try {
                    notificationRepository.insertNotification(
                        title = "Quỹ mới được tạo",
                        message = "Quỹ \"$trimmed\" đã được tạo thành công.",
                        timestamp = System.currentTimeMillis(),
                        type = "GOAL_CREATED"
                    )
                } catch (_: Throwable) {
                }
            }
        }
    }

    fun updateGoal(goal: GoalEntity) {
        viewModelScope.launch {
            goalDao.updateGoal(goal.copy(ownerId = userId))
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            goalDao.deleteGoal(goal.copy(ownerId = userId))
        }
    }

    /**
     * Insert a contribution for a goal. Type should be "Income" or "Expense".
     */
    fun insertContribution(goalName: String, amount: Double, date: String, type: String = "Expense") {
        viewModelScope.launch {
            try {
                val trimmedName = goalName.trim()
                // If this is a withdrawal (Income in app semantics), validate against current fund balance
                if (type == "Income") {
                    val currentBalance = try {
                        expenseDao.getBalanceForCategory(userId, trimmedName)
                    } catch (t: Throwable) {
                        // If DAO fails, treat as zero balance to avoid accidental overdraft
                        0.0
                    }
                    if (amount > currentBalance) {
                        // Emit UI error and do not proceed
                        _uiMessage.emit("Số tiền rút vượt quá số dư hiện tại của quỹ.")
                        return@launch
                    }
                }

                val e = ExpenseEntity(null, trimmedName, amount, date, type, ownerId = userId)
                expenseDao.insertExpense(e)
            } catch (t: Throwable) {
                // optionally emit a generic error for unexpected failures
                try { _uiMessage.emit("Có lỗi khi lưu giao dịch.") } catch (_: Throwable) {}
            }
        }
    }

    override fun onEvent(event: UiEvent) {
        // no-op
    }
}

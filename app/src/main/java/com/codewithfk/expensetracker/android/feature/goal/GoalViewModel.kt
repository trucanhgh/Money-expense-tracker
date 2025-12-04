package com.codewithfk.expensetracker.android.feature.goal

import androidx.lifecycle.viewModelScope
import com.codewithfk.expensetracker.android.base.BaseViewModel
import com.codewithfk.expensetracker.android.base.UiEvent
import com.codewithfk.expensetracker.android.data.dao.GoalDao
import com.codewithfk.expensetracker.android.data.dao.ExpenseDao
import com.codewithfk.expensetracker.android.data.model.GoalEntity
import com.codewithfk.expensetracker.android.data.model.ExpenseEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val goalDao: GoalDao,
    private val expenseDao: ExpenseDao
) : BaseViewModel() {

    val goals: Flow<List<GoalEntity>> = goalDao.getAllGoals()

    fun getContributionsForGoal(name: String, month: String? = null): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByCategory(name, month)
    }

    fun insertGoal(goal: GoalEntity) {
        viewModelScope.launch {
            val trimmed = goal.name.trim()
            if (trimmed.isNotEmpty()) {
                goalDao.insertGoal(GoalEntity(name = trimmed, targetAmount = goal.targetAmount, frequency = goal.frequency, reminderEnabled = goal.reminderEnabled))
            }
        }
    }

    fun updateGoal(goal: GoalEntity) {
        viewModelScope.launch {
            goalDao.updateGoal(goal)
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            goalDao.deleteGoal(goal)
        }
    }

    /**
     * Insert a contribution for a goal. Type should be "Income" or "Expense".
     */
    fun insertContribution(goalName: String, amount: Double, date: String, type: String = "Expense") {
        viewModelScope.launch {
            try {
                val e = ExpenseEntity(null, goalName.trim(), amount, date, type)
                expenseDao.insertExpense(e)
            } catch (_: Throwable) {
            }
        }
    }

    override fun onEvent(event: UiEvent) {
        // no-op
    }
}

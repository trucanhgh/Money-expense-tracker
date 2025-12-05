package com.codewithfk.expensetracker.android.feature.add_expense

import androidx.lifecycle.viewModelScope
import com.codewithfk.expensetracker.android.base.AddExpenseNavigationEvent
import com.codewithfk.expensetracker.android.base.BaseViewModel
import com.codewithfk.expensetracker.android.base.NavigationEvent
import com.codewithfk.expensetracker.android.base.UiEvent
import com.codewithfk.expensetracker.android.data.dao.ExpenseDao
import com.codewithfk.expensetracker.android.data.dao.CategoryDao
import com.codewithfk.expensetracker.android.data.dao.GoalDao
import com.codewithfk.expensetracker.android.data.model.ExpenseEntity
import com.codewithfk.expensetracker.android.data.model.CategoryEntity
import com.codewithfk.expensetracker.android.data.model.GoalEntity
import com.codewithfk.expensetracker.android.auth.CurrentUserProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    val dao: ExpenseDao,
    val categoryDao: CategoryDao,
    val goalDao: GoalDao,
    private val currentUserProvider: CurrentUserProvider
) : BaseViewModel() {

    private val userId: String = currentUserProvider.getUserId() ?: ""

    val categories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories(userId)
    val goals: Flow<List<GoalEntity>> = goalDao.getAllGoals(userId)

    suspend fun addExpense(expenseEntity: ExpenseEntity): Boolean {
        return try {
            // Ensure default category exists and assign if title blank
            val defaultName = "Khác"
            val title = if (expenseEntity.title.isBlank()) {
                val existing = categoryDao.getCategoryByName(userId, defaultName)
                if (existing == null) {
                    categoryDao.insertCategory(CategoryEntity(name = defaultName, ownerId = userId))
                }
                defaultName
            } else expenseEntity.title.trim()

            val toInsert = expenseEntity.copy(title = title, ownerId = userId)
            dao.insertExpense(toInsert)
            true
        } catch (_: Throwable) {
            false
        }
    }

    fun insertCategory(categoryEntity: CategoryEntity, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val trimmed = categoryEntity.name.trim()
                categoryDao.insertCategory(CategoryEntity(name = trimmed, ownerId = userId))
                withContext(Dispatchers.Main) { onResult(true) }
            } catch (_: Throwable) {
                withContext(Dispatchers.Main) { onResult(false) }
            }
        }
    }

    override fun onEvent(event: UiEvent) {
        when (event) {
            is AddExpenseUiEvent.OnAddExpenseClicked -> {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        val result = addExpense(event.expenseEntity)
                        if (result) {
                            _navigationEvent.emit(NavigationEvent.NavigateBack)
                        }
                    }
                }
            }

            is AddExpenseUiEvent.OnBackPressed -> {
                viewModelScope.launch {
                    _navigationEvent.emit(NavigationEvent.NavigateBack)
                }
            }

            is AddExpenseUiEvent.OnMenuClicked -> {
                viewModelScope.launch {
                    _navigationEvent.emit(AddExpenseNavigationEvent.MenuOpenedClicked)
                }
            }
        }
    }
}

sealed class AddExpenseUiEvent : UiEvent() {
    data class OnAddExpenseClicked(val expenseEntity: ExpenseEntity) : AddExpenseUiEvent()
    object OnBackPressed : AddExpenseUiEvent()
    object OnMenuClicked : AddExpenseUiEvent()
}

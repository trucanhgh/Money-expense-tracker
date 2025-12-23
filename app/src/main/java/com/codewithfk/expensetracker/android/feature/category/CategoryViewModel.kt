package com.codewithfk.expensetracker.android.feature.category

import androidx.lifecycle.viewModelScope
import com.codewithfk.expensetracker.android.base.BaseViewModel
import com.codewithfk.expensetracker.android.base.UiEvent
import com.codewithfk.expensetracker.android.data.dao.CategoryDao
import com.codewithfk.expensetracker.android.data.dao.ExpenseDao
import com.codewithfk.expensetracker.android.data.model.CategoryEntity
import com.codewithfk.expensetracker.android.data.model.CategorySummary
import com.codewithfk.expensetracker.android.data.model.ExpenseEntity
import com.codewithfk.expensetracker.android.auth.CurrentUserProvider
import com.codewithfk.expensetracker.android.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val currentUserProvider: CurrentUserProvider,
    private val notificationRepository: NotificationRepository
) : BaseViewModel() {

    private val userId: String = currentUserProvider.getUserId() ?: ""

    val categories = categoryDao.getAllCategories(userId)

    fun getCategoryTotals(month: String? = null): Flow<List<CategorySummary>> {
        return expenseDao.getCategoryTotals(userId, month)
    }

    fun getExpensesForCategory(name: String, month: String? = null): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByCategory(userId, name, month)
    }

    fun insertCategory(category: CategoryEntity) {
        viewModelScope.launch {
            val trimmed = category.name.trim()
            if (trimmed.isNotEmpty()) {
                // persist all fields including auto-transaction config
                val toInsert = CategoryEntity(
                    name = trimmed,
                    ownerId = userId,
                    isAutoTransactionEnabled = category.isAutoTransactionEnabled,
                    autoAmount = category.autoAmount,
                    autoType = category.autoType,
                    autoRepeatType = category.autoRepeatType,
                    autoDayOfWeek = category.autoDayOfWeek,
                    autoDayOfMonth = category.autoDayOfMonth,
                    lastAutoExecutedDate = category.lastAutoExecutedDate
                )
                categoryDao.insertCategory(toInsert)
                // create notification: category created
                try {
                    notificationRepository.insertNotification(
                        title = "Danh mục mới",
                        message = "Danh mục \"${toInsert.name}\" đã được tạo thành công.",
                        timestamp = System.currentTimeMillis(),
                        type = "CATEGORY_CREATED"
                    )
                } catch (_: Throwable) {
                    // ignore
                }
            }
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            try {
                val otherName = "Khác"
                // prevent deleting default
                if (category.name.equals(otherName, ignoreCase = true)) return@launch

                // Ensure default exists
                var other = categoryDao.getCategoryByName(userId, otherName)
                if (other == null) {
                    categoryDao.insertCategory(CategoryEntity(name = otherName, ownerId = userId))
                    other = categoryDao.getCategoryByName(userId, otherName)
                }

                // Reassign expenses from this category to default
                expenseDao.reassignExpensesToCategory(userId, category.name, other?.name ?: otherName)

                // Now delete the category
                categoryDao.deleteCategory(CategoryEntity(id = category.id, name = category.name, ownerId = userId))
            } catch (t: Throwable) {
                // ignore or log
            }
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            try {
                // if id exists, fetch current entity to check previous name
                val id = category.id
                val newNameTrimmed = category.name.trim()
                if (id != null) {
                    val existing = categoryDao.getCategoryById(userId, id)
                    if (existing != null && !existing.name.equals(newNameTrimmed, ignoreCase = true)) {
                        // do not allow renaming default to something else? If existing is default, block
                        val otherName = "Khác"
                        if (existing.name.equals(otherName, ignoreCase = true)) {
                            // don't rename default
                            return@launch
                        }
                        // reassign expenses from old name to new name
                        expenseDao.reassignExpensesToCategory(userId, existing.name, newNameTrimmed)
                    }
                }
                // Persist updated category including auto fields
                val toUpdate = CategoryEntity(
                    id = category.id,
                    name = newNameTrimmed,
                    ownerId = userId,
                    isAutoTransactionEnabled = category.isAutoTransactionEnabled,
                    autoAmount = category.autoAmount,
                    autoType = category.autoType,
                    autoRepeatType = category.autoRepeatType,
                    autoDayOfWeek = category.autoDayOfWeek,
                    autoDayOfMonth = category.autoDayOfMonth,
                    lastAutoExecutedDate = category.lastAutoExecutedDate
                )
                categoryDao.updateCategory(toUpdate)
            } catch (t: Throwable) {
                // ignore or log
            }
        }
    }

    // Toggle auto transaction enabled/disabled for a specific category id
    fun toggleAutoTransaction(categoryId: Int, enabled: Boolean) {
        viewModelScope.launch {
            try {
                val existing = categoryDao.getCategoryById(userId, categoryId)
                if (existing != null) {
                    val updated = existing.copy(isAutoTransactionEnabled = enabled)
                    categoryDao.updateCategory(updated)
                }
            } catch (_: Throwable) {
                // ignore
            }
        }
    }

    // Update auto configuration for a category (amount, type, repeat, day)
    fun updateAutoConfig(
        categoryId: Int,
        amount: Double,
        type: String,
        repeatType: String,
        dayOfWeek: Int?,
        dayOfMonth: Int?
    ) {
        viewModelScope.launch {
            try {
                val existing = categoryDao.getCategoryById(userId, categoryId)
                if (existing != null) {
                    val updated = existing.copy(
                        autoAmount = amount,
                        autoType = type,
                        autoRepeatType = repeatType,
                        autoDayOfWeek = dayOfWeek,
                        autoDayOfMonth = dayOfMonth
                    )
                    categoryDao.updateCategory(updated)
                }
            } catch (_: Throwable) {
                // ignore
            }
        }
    }

    override fun onEvent(event: UiEvent) {
        // No-op for now
    }
}

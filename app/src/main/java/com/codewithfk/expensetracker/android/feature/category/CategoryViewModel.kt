package com.codewithfk.expensetracker.android.feature.category

import androidx.lifecycle.viewModelScope
import com.codewithfk.expensetracker.android.base.BaseViewModel
import com.codewithfk.expensetracker.android.base.UiEvent
import com.codewithfk.expensetracker.android.data.dao.CategoryDao
import com.codewithfk.expensetracker.android.data.dao.ExpenseDao
import com.codewithfk.expensetracker.android.data.model.CategoryEntity
import com.codewithfk.expensetracker.android.data.model.CategorySummary
import com.codewithfk.expensetracker.android.data.model.ExpenseEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao
) : BaseViewModel() {

    val categories = categoryDao.getAllCategories()

    fun getCategoryTotals(month: String? = null): Flow<List<CategorySummary>> {
        return expenseDao.getCategoryTotals(month)
    }

    fun getExpensesForCategory(name: String, month: String? = null): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByCategory(name, month)
    }

    fun insertCategory(category: CategoryEntity) {
        viewModelScope.launch {
            val trimmed = category.name.trim()
            if (trimmed.isNotEmpty()) {
                categoryDao.insertCategory(CategoryEntity(name = trimmed))
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
                var other = categoryDao.getCategoryByName(otherName)
                if (other == null) {
                    categoryDao.insertCategory(CategoryEntity(name = otherName))
                    other = categoryDao.getCategoryByName(otherName)
                }

                // Reassign expenses from this category to default
                expenseDao.reassignExpensesToCategory(category.name, other?.name ?: otherName)

                // Now delete the category
                categoryDao.deleteCategory(category)
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
                    val existing = categoryDao.getCategoryById(id)
                    if (existing != null && !existing.name.equals(newNameTrimmed, ignoreCase = true)) {
                        // do not allow renaming default to something else? If existing is default, block
                        val otherName = "Khác"
                        if (existing.name.equals(otherName, ignoreCase = true)) {
                            // don't rename default
                            return@launch
                        }
                        // reassign expenses from old name to new name
                        expenseDao.reassignExpensesToCategory(existing.name, newNameTrimmed)
                    }
                }
                categoryDao.updateCategory(CategoryEntity(id = category.id, name = newNameTrimmed))
            } catch (t: Throwable) {
                // ignore or log
            }
        }
    }

    override fun onEvent(event: UiEvent) {
        // No-op for now
    }
}

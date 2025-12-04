package com.codewithfk.expensetracker.android.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.codewithfk.expensetracker.android.data.model.ExpenseEntity
import com.codewithfk.expensetracker.android.data.model.ExpenseSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {


    @Query("SELECT * FROM expense_table")
    fun getAllExpense(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expense_table WHERE type = 'Expense' ORDER BY amount DESC LIMIT 5")
    fun getTopExpenses(): Flow<List<ExpenseEntity>>


    @Query("SELECT type, date, SUM(amount) AS total_amount FROM expense_table where type = :type GROUP BY type, date ORDER BY date")
    fun getAllExpenseByDate(type: String = "Expense"): Flow<List<ExpenseSummary>>

    @Insert
    suspend fun insertExpense(expenseEntity: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expenseEntity: ExpenseEntity)

    @Update
    suspend fun updateExpense(expenseEntity: ExpenseEntity)

    // Reassign expenses: update title from oldName to newName (case-insensitive match trimmed)
    @Query("UPDATE expense_table SET title = :newName WHERE lower(trim(title)) = lower(trim(:oldName))")
    suspend fun reassignExpensesToCategory(oldName: String, newName: String)

    // Get total expense per category. month filter should be in format MM/YYYY (e.g., "11/2025").
    @Query(
        """
        SELECT c.id as id, c.name as name, IFNULL(SUM(CASE WHEN e.type = 'Income' THEN e.amount ELSE -e.amount END), 0) as total
        FROM category_table c
        LEFT JOIN expense_table e ON (lower(trim(e.title)) = lower(trim(c.name)) OR lower(e.title) LIKE '%' || lower(c.name) || '%') AND (:month IS NULL OR substr(e.date,4) = :month)
        GROUP BY c.id, c.name
        ORDER BY total DESC
        """
    )
    fun getCategoryTotals(month: String? = null): Flow<List<com.codewithfk.expensetracker.android.data.model.CategorySummary>>

    // Get expenses belonging to a category, optionally filtered by month (MM/YYYY)
    @Query(
        "SELECT * FROM expense_table WHERE (lower(trim(title)) = lower(trim(:categoryName)) OR lower(title) LIKE '%' || lower(:categoryName) || '%') AND (:month IS NULL OR substr(date,4) = :month) ORDER BY date DESC"
    )
    fun getExpensesByCategory(categoryName: String, month: String? = null): Flow<List<ExpenseEntity>>
}
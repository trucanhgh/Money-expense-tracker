package com.codewithfk.expensetracker.android.data.repository

import com.codewithfk.expensetracker.android.data.dao.CategoryDao
import com.codewithfk.expensetracker.android.data.dao.ExpenseDao
import com.codewithfk.expensetracker.android.data.model.ExpenseEntity
import com.codewithfk.expensetracker.android.data.model.CategoryEntity
import com.codewithfk.expensetracker.android.auth.CurrentUserProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val currentUserProvider: CurrentUserProvider
) {
    private val userId: String = currentUserProvider.getUserId() ?: ""
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // New: API requested by the spec
    suspend fun processCategoryAutoTransactions(today: LocalDate) {
        // convert LocalDate to Calendar
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, today.year)
        cal.set(Calendar.MONTH, today.monthValue - 1)
        cal.set(Calendar.DAY_OF_MONTH, today.dayOfMonth)
        processCategoryAutoTransactions(cal)
    }

    // Existing no-arg for worker convenience; delegates to calendar-based impl
    suspend fun processCategoryAutoTransactions() {
        processCategoryAutoTransactions(Calendar.getInstance())
    }

    private suspend fun processCategoryAutoTransactions(todayCal: Calendar) {
        // Fetch auto-enabled categories for the current user
        val categories: List<CategoryEntity> = categoryDao.getAutoEnabledCategories(userId)
        val todayDate = todayCal.time
        val todayStr = dateFormat.format(todayDate)
        val todayWeek = todayCal.get(Calendar.WEEK_OF_YEAR)
        val todayYear = todayCal.get(Calendar.YEAR)
        val todayDayOfMonth = todayCal.get(Calendar.DAY_OF_MONTH)
        val todayDayOfWeekCalendar = todayCal.get(Calendar.DAY_OF_WEEK) // 1=Sunday,2=Monday,...7=Saturday

        for (cat in categories) {
            try {
                if (!cat.isAutoTransactionEnabled) continue
                if (cat.autoAmount <= 0.0) continue

                var shouldExecute = false

                when (cat.autoRepeatType.uppercase(Locale.getDefault())) {
                    "WEEKLY" -> {
                        // Map stored day (1=Monday..7=Sunday) to Calendar day (Sunday=1..Saturday=7)
                        val desiredDayCalendar = cat.autoDayOfWeek?.let { d -> if (d == 7) Calendar.SUNDAY else d + 1 } ?: todayDayOfWeekCalendar
                        if (todayDayOfWeekCalendar != desiredDayCalendar) {
                            shouldExecute = false
                        } else {
                            val lastStr = cat.lastAutoExecutedDate
                            if (lastStr == null) {
                                shouldExecute = true
                            } else {
                                val lastDate = try { dateFormat.parse(lastStr) } catch (e: Exception) { null }
                                if (lastDate == null) {
                                    shouldExecute = true
                                } else {
                                    val lastCal = Calendar.getInstance().apply { time = lastDate }
                                    val lastWeek = lastCal.get(Calendar.WEEK_OF_YEAR)
                                    val lastYear = lastCal.get(Calendar.YEAR)
                                    if (lastWeek != todayWeek || lastYear != todayYear) {
                                        shouldExecute = true
                                    }
                                }
                            }
                        }
                    }

                    "MONTHLY" -> {
                        val desiredDay = cat.autoDayOfMonth ?: todayDayOfMonth.coerceAtMost(28)
                        if (todayDayOfMonth != desiredDay) {
                            shouldExecute = false
                        } else {
                            val lastStr = cat.lastAutoExecutedDate
                            if (lastStr == null) {
                                shouldExecute = true
                            } else {
                                val lastDate = try { dateFormat.parse(lastStr) } catch (e: Exception) { null }
                                if (lastDate == null) {
                                    shouldExecute = true
                                } else {
                                    val lastCal = Calendar.getInstance().apply { time = lastDate }
                                    val lastMonth = lastCal.get(Calendar.MONTH)
                                    val lastYear = lastCal.get(Calendar.YEAR)
                                    if (lastMonth != todayCal.get(Calendar.MONTH) || lastYear != todayYear) {
                                        shouldExecute = true
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        shouldExecute = false
                    }
                }

                if (shouldExecute) {
                    // Insert ExpenseEntity
                    val expense = ExpenseEntity(
                        id = null,
                        title = cat.name,
                        amount = cat.autoAmount,
                        date = todayStr,
                        type = cat.autoType,
                        note = "Auto transaction",
                        ownerId = userId
                    )
                    expenseDao.insertExpense(expense)

                    // Update category's lastAutoExecutedDate
                    val updated = CategoryEntity(
                        id = cat.id,
                        name = cat.name,
                        ownerId = cat.ownerId,
                        isAutoTransactionEnabled = cat.isAutoTransactionEnabled,
                        autoAmount = cat.autoAmount,
                        autoType = cat.autoType,
                        autoRepeatType = cat.autoRepeatType,
                        autoDayOfWeek = cat.autoDayOfWeek,
                        autoDayOfMonth = cat.autoDayOfMonth,
                        lastAutoExecutedDate = todayStr
                    )
                    categoryDao.updateCategory(updated)
                }
            } catch (t: Throwable) {
                // ignore individual category failures to keep processing other categories
            }
        }
    }
}

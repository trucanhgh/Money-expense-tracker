package com.codewithfk.expensetracker.android.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.codewithfk.expensetracker.android.data.ExpenseDatabase
import com.codewithfk.expensetracker.android.data.repository.CategoryRepository
import com.codewithfk.expensetracker.android.data.repository.NotificationRepository
import com.codewithfk.expensetracker.android.auth.CurrentUserProvider
import java.time.LocalDate

class AutoTransactionWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val db = ExpenseDatabase.getInstance(applicationContext)
            val categoryDao = db.categoryDao()
            val expenseDao = db.expenseDao()
            val currentUserProvider = CurrentUserProvider(applicationContext)

            // Create NotificationRepository manually for the worker (no Hilt in worker here)
            val notificationRepo = NotificationRepository(db.notificationDao(), currentUserProvider)

            val repo = CategoryRepository(categoryDao, expenseDao, currentUserProvider, notificationRepo)

            val today = LocalDate.now()
            repo.processCategoryAutoTransactions(today)

            Result.success()
        } catch (t: Throwable) {
            Result.failure()
        }
    }
}

package com.codewithfk.expensetracker.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExpenseTrackerApp: Application() {
    override fun onCreate() {
        super.onCreate()

        // TODO: Schedule the AutoTransactionWorker here once WorkManager dependency is available.
        // Example:
        // val workRequest = PeriodicWorkRequestBuilder<AutoTransactionWorker>(1, TimeUnit.DAYS).build()
        // WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        //     "auto_transactions",
        //     ExistingPeriodicWorkPolicy.KEEP,
        //     workRequest
        // )
    }
}
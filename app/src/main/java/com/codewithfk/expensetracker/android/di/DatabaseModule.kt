package com.codewithfk.expensetracker.android.di

import android.content.Context
import com.codewithfk.expensetracker.android.data.ExpenseDatabase
import com.codewithfk.expensetracker.android.data.dao.ExpenseDao
import com.codewithfk.expensetracker.android.data.dao.CategoryDao
import com.codewithfk.expensetracker.android.data.dao.UserDao
import com.codewithfk.expensetracker.android.data.dao.GoalDao
import com.codewithfk.expensetracker.android.data.dao.NotificationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context, ): ExpenseDatabase {
        return ExpenseDatabase.getInstance(context)
    }

    @Provides
    fun provideExpenseDao(database: ExpenseDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    fun provideCategoryDao(database: ExpenseDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    fun provideUserDao(database: ExpenseDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideGoalDao(database: ExpenseDatabase): GoalDao {
        return database.goalDao()
    }

    @Provides
    fun provideNotificationDao(database: ExpenseDatabase): NotificationDao {
        return database.notificationDao()
    }
}

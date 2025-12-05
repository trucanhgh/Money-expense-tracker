@file:Suppress("unused")
package com.codewithfk.expensetracker.android.feature.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import com.codewithfk.expensetracker.android.auth.CurrentUserProvider
import com.codewithfk.expensetracker.android.data.dao.UserDao
import com.codewithfk.expensetracker.android.data.model.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userDao: UserDao,
    @ApplicationContext private val context: Context,
    private val currentUserProvider: CurrentUserProvider
) : ViewModel() {

    private val prefName = "auth_prefs"
    private val keyRemember = "remember_username"
    private val keyCurrent = "current_username"

    suspend fun registerUser(username: String, password: String): Boolean {
        return try {
            val existing = userDao.getUserByUsername(username)
            if (existing != null) return false
            userDao.insertUser(UserEntity(username = username, password = password))
            // set current user for app scope
            currentUserProvider.setUserId(username)
            true
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun loginUser(username: String, password: String): Boolean {
        return try {
            val user = userDao.getUserByUsername(username)
            if (user == null) return false
            val ok = user.password == password
            if (ok) currentUserProvider.setUserId(username)
            return ok
        } catch (_: Throwable) {
            false
        }
    }

    fun saveRememberUsername(username: String) {
        val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        prefs.edit().putString(keyRemember, username).apply()
    }

    fun clearRemember() {
        val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        prefs.edit().remove(keyRemember).apply()
    }

    fun getRememberedUsername(): String? {
        val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        return prefs.getString(keyRemember, null)
    }

    // Session management (current logged-in user for this run)
    fun saveCurrentUser(username: String) {
        val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        prefs.edit().putString(keyCurrent, username).apply()
        currentUserProvider.setUserId(username)
    }

    fun getCurrentUser(): String? {
        // prefer CurrentUserProvider
        return currentUserProvider.getUserId() ?: context.getSharedPreferences(prefName, Context.MODE_PRIVATE).getString(keyCurrent, null)
    }

    fun clearCurrentUser() {
        val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        prefs.edit().remove(keyCurrent).apply()
        currentUserProvider.setUserId(null)
    }
}

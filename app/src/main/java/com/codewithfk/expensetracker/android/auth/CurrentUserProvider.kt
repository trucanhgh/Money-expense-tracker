package com.codewithfk.expensetracker.android.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple provider to read the currently-signed-in user's id from SharedPreferences.
 * Currently uses the existing auth_prefs/current_username mechanism. Later can be
 * updated to read FirebaseAuth.currentUser?.uid instead.
 */
@Singleton
class CurrentUserProvider @Inject constructor(@ApplicationContext private val context: Context) {
    private val prefName = "auth_prefs"
    private val keyCurrent = "current_username"

    fun getUserId(): String? {
        val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        return prefs.getString(keyCurrent, null)
    }

    fun setUserId(userId: String?) {
        val prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        if (userId == null) prefs.edit().remove(keyCurrent).apply() else prefs.edit().putString(keyCurrent, userId).apply()
    }
}


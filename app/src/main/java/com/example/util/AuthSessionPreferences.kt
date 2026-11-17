package com.example.util

import android.content.Context

object AuthSessionPreferences {
    private const val PREFS_NAME = "zam_khata_prefs"
    private const val KEY_CURRENT_USER_ID = "current_user_id"
    const val NO_USER_ID = -1L

    fun getCurrentUserId(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_CURRENT_USER_ID, NO_USER_ID)
    }

    fun setCurrentUserId(context: Context, userId: Long) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_CURRENT_USER_ID, userId)
            .apply()
    }

    fun clearSession(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_CURRENT_USER_ID)
            .apply()
    }
}

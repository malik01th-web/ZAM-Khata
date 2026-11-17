package com.example.util

import android.content.Context

enum class AppThemeMode(val code: String) {
    DARK("dark"),
    LIGHT("light");

    val isDark: Boolean
        get() = this == DARK

    companion object {
        fun fromCode(code: String?): AppThemeMode? = entries.firstOrNull { it.code == code }
    }
}

object ThemePreferences {
    private const val PREFS_NAME = "zam_khata_prefs"
    private const val KEY_THEME_MODE = "app_theme_mode"

    fun getThemeMode(context: Context): AppThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = AppThemeMode.fromCode(prefs.getString(KEY_THEME_MODE, null))
        return saved ?: AppThemeMode.DARK
    }

    fun setThemeMode(context: Context, mode: AppThemeMode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, mode.code)
            .apply()
    }
}

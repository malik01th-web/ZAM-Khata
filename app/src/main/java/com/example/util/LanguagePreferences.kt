package com.example.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

enum class AppLanguage(val code: String) {
    ENGLISH("en"),
    URDU("ur");

    val isRtl: Boolean
        get() = this == URDU

    fun toLocale(): Locale = when (this) {
        ENGLISH -> Locale.ENGLISH
        URDU -> Locale.forLanguageTag("ur-PK")
    }

    companion object {
        fun fromCode(code: String?): AppLanguage? = entries.firstOrNull { it.code == code }
    }
}

object LanguagePreferences {
    private const val PREFS_NAME = "zam_khata_prefs"
    private const val KEY_LANGUAGE = "app_language"

    fun getSelectedLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = AppLanguage.fromCode(prefs.getString(KEY_LANGUAGE, null))
        if (saved != null) return saved
        val systemLang = Locale.getDefault().language
        return if (systemLang == "ur") AppLanguage.URDU else AppLanguage.ENGLISH
    }

    fun setSelectedLanguage(context: Context, language: AppLanguage) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language.code)
            .apply()
    }

    fun wrapContext(base: Context): Context {
        val language = getSelectedLanguage(base)
        val locale = language.toLocale()
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return base.createConfigurationContext(config)
    }
}

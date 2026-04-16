package com.vikash.pokertrainer.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Persists the user's theme preference (light vs dark) across app launches.
 */
class ThemePreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, true)
        set(value) {
            prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
        }

    companion object {
        private const val PREFS_NAME = "poker_trainer_theme"
        private const val KEY_DARK_MODE = "is_dark_mode"
    }
}

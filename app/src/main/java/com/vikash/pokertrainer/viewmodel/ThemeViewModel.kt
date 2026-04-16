package com.vikash.pokertrainer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.vikash.pokertrainer.data.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Exposes the current theme (light/dark) as observable state and persists changes.
 */
class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = ThemePreferences(application)

    private val _isDarkMode = MutableStateFlow(prefs.isDarkMode)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleTheme() {
        val next = !_isDarkMode.value
        prefs.isDarkMode = next
        _isDarkMode.value = next
    }

    fun setDarkMode(dark: Boolean) {
        if (_isDarkMode.value == dark) return
        prefs.isDarkMode = dark
        _isDarkMode.value = dark
    }
}

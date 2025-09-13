package com.example.noteproject.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * @deprecated This class is deprecated. Use DataStoreThemeManager instead.
 * This class is kept for migration purposes only.
 */
@Deprecated("Use DataStoreThemeManager instead")
class ThemePreferenceManager(context: Context) {
    
    companion object {
        private const val PREF_NAME = "theme_preferences"
        private const val KEY_IS_DARK_THEME = "is_dark_theme"
        private const val DEFAULT_DARK_THEME = false
    }
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )
    
    fun isDarkTheme(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_DARK_THEME, DEFAULT_DARK_THEME)
    }
    
    fun setDarkTheme(isDarkTheme: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_DARK_THEME, isDarkTheme)
            .apply()
    }
    
    fun toggleTheme(): Boolean {
        val newTheme = !isDarkTheme()
        setDarkTheme(newTheme)
        return newTheme
    }
}

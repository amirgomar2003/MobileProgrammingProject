package com.example.noteproject.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Extension property to create DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

class DataStoreThemeManager(private val context: Context) {
    
    companion object {
        private val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
    }
    
    private suspend fun migrateFromSharedPrefsIfNeeded() {
        val currentPrefs = context.dataStore.data.map { it }.first()
        
        // Only migrate if DataStore doesn't have the key yet
        if (!currentPrefs.contains(IS_DARK_THEME)) {
            val sharedPrefs = context.getSharedPreferences("theme_preferences", Context.MODE_PRIVATE)
            val oldValue = sharedPrefs.getBoolean("is_dark_theme", false)
            
            // Set the migrated value
            context.dataStore.edit { preferences ->
                preferences[IS_DARK_THEME] = oldValue
            }
        }
    }
    
    val isDarkTheme: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_THEME] ?: false
        }
    
    suspend fun setDarkTheme(isDarkTheme: Boolean) {
        // Ensure migration happens before any write
        migrateFromSharedPrefsIfNeeded()
        
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_THEME] = isDarkTheme
        }
    }
    
    suspend fun initialize() {
        migrateFromSharedPrefsIfNeeded()
    }
}

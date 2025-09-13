package com.example.noteproject.ui.theme

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.noteproject.data.local.ThemePreferenceManager

/**
 * @deprecated This function is deprecated. Use rememberDataStoreThemeState instead.
 * This function uses SharedPreferences which has been replaced with DataStore.
 */
@Deprecated("Use rememberDataStoreThemeState instead", ReplaceWith("rememberDataStoreThemeState(context)"))
@Composable
fun rememberThemeState(context: Context = LocalContext.current): MutableState<Boolean> {
    val themePreferenceManager = remember { ThemePreferenceManager(context) }
    
    // Initialize state with the saved preference
    val themeState = remember { 
        mutableStateOf(themePreferenceManager.isDarkTheme()) 
    }
    
    // Create a derived state that automatically saves when changed
    return object : MutableState<Boolean> {
        override var value: Boolean
            get() = themeState.value
            set(value) {
                themeState.value = value
                themePreferenceManager.setDarkTheme(value)
            }
        
        override fun component1(): Boolean = value
        override fun component2(): (Boolean) -> Unit = { value = it }
    }
}

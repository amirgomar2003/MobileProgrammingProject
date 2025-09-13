package com.example.noteproject.ui.theme

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.noteproject.data.local.DataStoreThemeManager
import kotlinx.coroutines.launch

@Composable
fun rememberDataStoreThemeState(context: Context = LocalContext.current): MutableState<Boolean> {
    val dataStoreThemeManager = remember { DataStoreThemeManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    // Initialize migration on first composition
    LaunchedEffect(dataStoreThemeManager) {
        dataStoreThemeManager.initialize()
    }
    
    // Collect the theme state from DataStore
    val themeStateFromDataStore by dataStoreThemeManager.isDarkTheme.collectAsState(initial = false)
    
    // Create a mutable state and keep it synchronized with DataStore
    val mutableState = remember { mutableStateOf(false) }
    
    // Update the mutable state when DataStore value changes
    LaunchedEffect(themeStateFromDataStore) {
        mutableState.value = themeStateFromDataStore
    }
    
    // Return a derived mutable state that also updates DataStore
    return object : MutableState<Boolean> {
        override var value: Boolean
            get() = mutableState.value
            set(newValue) {
                // Update local state immediately for responsive UI
                mutableState.value = newValue
                // Save to DataStore asynchronously
                coroutineScope.launch {
                    dataStoreThemeManager.setDarkTheme(newValue)
                }
            }
        
        override fun component1(): Boolean = value
        override fun component2(): (Boolean) -> Unit = { value = it }
    }
}

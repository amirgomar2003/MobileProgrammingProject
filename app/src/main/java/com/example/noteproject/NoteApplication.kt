package com.example.noteproject

import android.app.Application
import androidx.work.Configuration

class NoteApplication : Application(), Configuration.Provider {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize WorkManager with custom configuration if needed
        // This is handled automatically by the manifest provider,
        // but we can customize it here if needed
    }
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}

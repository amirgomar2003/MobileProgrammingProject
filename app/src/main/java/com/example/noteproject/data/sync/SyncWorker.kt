package com.example.noteproject.data.sync

import android.content.Context
import androidx.work.*
import com.example.noteproject.data.api.ApiClient
import com.example.noteproject.data.local.NetworkMonitor
import com.example.noteproject.data.local.NotesDatabase
import com.example.noteproject.data.local.TokenManager
import com.example.noteproject.data.repository.OfflineFirstNotesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        const val WORK_NAME = "notes_sync_work"
        const val PERIODIC_SYNC_WORK = "periodic_sync_work"
        
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
            
            val periodicWork = PeriodicWorkRequestBuilder<SyncWorker>(
                15, TimeUnit.MINUTES // Sync every 15 minutes when conditions are met
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10, TimeUnit.SECONDS
                )
                .build()
            
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    PERIODIC_SYNC_WORK,
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicWork
                )
        }
        
        fun scheduleImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            
            val immediateWork = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10, TimeUnit.SECONDS
                )
                .build()
            
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    immediateWork
                )
        }
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val tokenManager = TokenManager(applicationContext)
            
            // Don't sync if not logged in
            if (!tokenManager.isLoggedIn()) {
                return@withContext Result.success()
            }
            
            val database = NotesDatabase.getDatabase(applicationContext)
            val networkMonitor = NetworkMonitor(applicationContext)
            val apiClient = ApiClient(tokenManager)
            
            val repository = OfflineFirstNotesRepository(
                context = applicationContext,
                apiClient = apiClient,
                tokenManager = tokenManager,
                database = database,
                networkMonitor = networkMonitor
            )
            
            val result = repository.syncWithServer()
            
            when (result) {
                is com.example.noteproject.data.repository.ApiResult.Success -> {
                    Result.success()
                }
                is com.example.noteproject.data.repository.ApiResult.Error -> {
                    Result.retry()
                }
                is com.example.noteproject.data.repository.ApiResult.NetworkError -> {
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

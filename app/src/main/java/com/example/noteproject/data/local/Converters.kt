package com.example.noteproject.data.local

import androidx.room.TypeConverter
import com.example.noteproject.data.local.entity.SyncStatus
import com.example.noteproject.data.local.entity.OperationType

class Converters {
    
    @TypeConverter
    fun fromSyncStatus(syncStatus: SyncStatus): String {
        return syncStatus.name
    }
    
    @TypeConverter
    fun toSyncStatus(syncStatus: String): SyncStatus {
        return SyncStatus.valueOf(syncStatus)
    }
    
    @TypeConverter
    fun fromOperationType(operationType: OperationType): String {
        return operationType.name
    }
    
    @TypeConverter
    fun toOperationType(operationType: String): OperationType {
        return OperationType.valueOf(operationType)
    }
}

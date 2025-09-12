package com.example.noteproject.data.local.dao

import androidx.room.*
import com.example.noteproject.data.local.entity.PendingOperationEntity
import com.example.noteproject.data.local.entity.OperationType

@Dao
interface PendingOperationDao {
    
    @Query("SELECT * FROM pending_operations ORDER BY timestamp ASC")
    suspend fun getAllPendingOperations(): List<PendingOperationEntity>
    
    @Query("SELECT * FROM pending_operations WHERE operationType = :operationType ORDER BY timestamp ASC")
    suspend fun getPendingOperationsByType(operationType: OperationType): List<PendingOperationEntity>
    
    @Insert
    suspend fun insertPendingOperation(operation: PendingOperationEntity): Long
    
    @Delete
    suspend fun deletePendingOperation(operation: PendingOperationEntity)
    
    @Query("DELETE FROM pending_operations WHERE id = :operationId")
    suspend fun deletePendingOperationById(operationId: Long)
    
    @Query("DELETE FROM pending_operations WHERE noteId = :noteId")
    suspend fun deletePendingOperationsForNote(noteId: Int)
    
    @Update
    suspend fun updatePendingOperation(operation: PendingOperationEntity)
    
    @Query("UPDATE pending_operations SET retryCount = retryCount + 1 WHERE id = :operationId")
    suspend fun incrementRetryCount(operationId: Long)
    
    @Query("DELETE FROM pending_operations")
    suspend fun clearAll()
}

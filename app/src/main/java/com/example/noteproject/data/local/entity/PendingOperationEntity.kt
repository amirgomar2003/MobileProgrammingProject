package com.example.noteproject.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_operations")
data class PendingOperationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: Int?, // null for new notes created offline
    val operationType: OperationType,
    val title: String?,
    val description: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)

enum class OperationType {
    CREATE,
    UPDATE,
    DELETE
}

package com.example.noteproject.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.noteproject.data.model.CreateNoteRequest
import com.example.noteproject.data.model.NoteResponse
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false,
    val isDirty: Boolean = false, // Indicates local changes not synced to server
    val isLocalOnly: Boolean = false, // For new notes created offline
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

enum class SyncStatus {
    SYNCED,       // Up to date with server
    PENDING_UPLOAD, // Local changes need to be uploaded
    PENDING_DELETE, // Marked for deletion, needs server sync
    CONFLICT      // Sync conflict detected
}

// Extension functions for conversion between entities and API models
fun NoteEntity.toNoteResponse(): NoteResponse {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
    return NoteResponse(
        id = this.id,
        title = this.title,
        description = this.description,
        createdAt = dateFormat.format(Date(this.createdAt)),
        updatedAt = dateFormat.format(Date(this.updatedAt))
    )
}

fun NoteEntity.toCreateNoteRequest(): CreateNoteRequest {
    return CreateNoteRequest(
        title = this.title,
        description = this.description
    )
}

fun NoteResponse.toNoteEntity(isDirty: Boolean = false, isLocalOnly: Boolean = false): NoteEntity {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
    val fallbackFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    
    val createdAtMillis = try {
        dateFormat.parse(this.createdAt)?.time ?: fallbackFormat.parse(this.createdAt)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
    
    val updatedAtMillis = try {
        dateFormat.parse(this.updatedAt)?.time ?: fallbackFormat.parse(this.updatedAt)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
    
    return NoteEntity(
        id = this.id,
        title = this.title,
        description = this.description,
        createdAt = createdAtMillis,
        updatedAt = updatedAtMillis,
        isDirty = isDirty,
        isLocalOnly = isLocalOnly,
        syncStatus = if (isDirty) SyncStatus.PENDING_UPLOAD else SyncStatus.SYNCED
    )
}

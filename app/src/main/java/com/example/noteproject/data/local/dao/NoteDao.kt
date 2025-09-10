package com.example.noteproject.data.local.dao

import androidx.room.*
import com.example.noteproject.data.local.entity.NoteEntity
import com.example.noteproject.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>
    
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY updatedAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getNotesPage(limit: Int, offset: Int): List<NoteEntity>
    
    @Query("SELECT * FROM notes WHERE id = :noteId AND isDeleted = 0")
    suspend fun getNoteById(noteId: Int): NoteEntity?
    
    @Query("SELECT * FROM notes WHERE id = :noteId AND isDeleted = 0")
    fun getNoteByIdFlow(noteId: Int): Flow<NoteEntity?>
    
    @Query("""
        SELECT * FROM notes 
        WHERE isDeleted = 0 
        AND (title LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%')
        ORDER BY updatedAt DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchNotes(searchQuery: String, limit: Int, offset: Int): List<NoteEntity>
    
    @Query("""
        SELECT * FROM notes 
        WHERE isDeleted = 0 
        AND (title LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%')
        ORDER BY updatedAt DESC
    """)
    fun searchNotesFlow(searchQuery: String): Flow<List<NoteEntity>>
    
    @Query("SELECT COUNT(*) FROM notes WHERE isDeleted = 0")
    suspend fun getTotalNotesCount(): Int
    
    @Query("""
        SELECT COUNT(*) FROM notes 
        WHERE isDeleted = 0 
        AND (title LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%')
    """)
    suspend fun getSearchResultsCount(searchQuery: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)
    
    @Update
    suspend fun updateNote(note: NoteEntity)
    
    @Query("UPDATE notes SET isDeleted = 1, syncStatus = :syncStatus WHERE id = :noteId")
    suspend fun markNoteAsDeleted(noteId: Int, syncStatus: SyncStatus = SyncStatus.PENDING_DELETE)
    
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: Int)
    
    @Query("DELETE FROM notes WHERE isDeleted = 1 AND syncStatus = :syncStatus")
    suspend fun deleteMarkedNotes(syncStatus: SyncStatus = SyncStatus.SYNCED)
    
    // Sync-related queries
    @Query("SELECT * FROM notes WHERE syncStatus = :syncStatus")
    suspend fun getNotesBySyncStatus(syncStatus: SyncStatus): List<NoteEntity>
    
    @Query("SELECT * FROM notes WHERE isDirty = 1 OR isLocalOnly = 1")
    suspend fun getUnsyncedNotes(): List<NoteEntity>
    
    @Query("UPDATE notes SET isDirty = 0, isLocalOnly = 0, syncStatus = :syncStatus WHERE id = :noteId")
    suspend fun markNoteSynced(noteId: Int, syncStatus: SyncStatus = SyncStatus.SYNCED)
    
    @Query("UPDATE notes SET syncStatus = :syncStatus WHERE id = :noteId")
    suspend fun updateSyncStatus(noteId: Int, syncStatus: SyncStatus)
    
    // For generating temporary IDs for offline-created notes
    @Query("SELECT MIN(id) FROM notes WHERE id < 0")
    suspend fun getNextTempId(): Int?
    
    @Transaction
    suspend fun insertNoteWithTempId(title: String, description: String): NoteEntity {
        val tempId = (getNextTempId() ?: 0) - 1
        val note = NoteEntity(
            id = tempId,
            title = title,
            description = description,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isDirty = true,
            isLocalOnly = true,
            syncStatus = SyncStatus.PENDING_UPLOAD
        )
        insertNote(note)
        return note
    }
    
    @Query("DELETE FROM notes")
    suspend fun clearAll()
}

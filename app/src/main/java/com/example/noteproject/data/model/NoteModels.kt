package com.example.noteproject.data.model

import com.google.gson.annotations.SerializedName

// Request models
data class CreateNoteRequest(
    val title: String,
    val description: String
)

data class UpdateNoteRequest(
    val title: String?,
    val description: String?
)

// Response models
data class NoteResponse(
    val id: Int,
    val title: String,
    val description: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at") 
    val updatedAt: String
)

data class PaginatedNotesResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<NoteResponse>
)

// Search and filter parameters
data class NoteFilterParams(
    val title: String? = null,
    val description: String? = null,
    val updatedGte: String? = null,
    val updatedLte: String? = null,
    val page: Int? = null,
    val pageSize: Int? = null
)

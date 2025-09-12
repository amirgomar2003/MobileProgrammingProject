package com.example.noteproject.data.api

import com.example.noteproject.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface NotesApiService {
    
    @GET("api/notes/")
    suspend fun getNotes(
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null
    ): Response<PaginatedNotesResponse>
    
    @GET("api/notes/{id}/")
    suspend fun getNoteById(@Path("id") id: Int): Response<NoteResponse>
    
    @POST("api/notes/")
    suspend fun createNote(@Body request: CreateNoteRequest): Response<NoteResponse>
    
    @PUT("api/notes/{id}/")
    suspend fun updateNote(
        @Path("id") id: Int,
        @Body request: UpdateNoteRequest
    ): Response<NoteResponse>
    
    @PATCH("api/notes/{id}/")
    suspend fun partialUpdateNote(
        @Path("id") id: Int,
        @Body request: UpdateNoteRequest
    ): Response<NoteResponse>
    
    @DELETE("api/notes/{id}/")
    suspend fun deleteNote(@Path("id") id: Int): Response<Unit>
    

    
    @GET("api/notes/filter")
    suspend fun searchNotes(
        @Query("title") title: String? = null,
        @Query("description") description: String? = null,
        @Query("updated__gte") updatedGte: String? = null,
        @Query("updated__lte") updatedLte: String? = null,
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null
    ): Response<PaginatedNotesResponse>
}

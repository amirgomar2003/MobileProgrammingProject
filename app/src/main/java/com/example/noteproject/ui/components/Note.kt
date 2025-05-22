package com.example.noteproject.ui.components

data class Note(
    val id: Int,
    var header: String,
    var body: String,
    var lastEdited: Long
)

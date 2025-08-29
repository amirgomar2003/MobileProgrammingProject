package com.example.noteproject

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.text.font.FontWeight
import com.example.noteproject.ui.components.Note

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorPage(
    note: Note,
    onHeaderChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    showDeleteDialog: Boolean,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onSave: (title: String, body: String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var header by remember { mutableStateOf(note.header) }
    var body by remember { mutableStateOf(note.body) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = { 
                                // Validate that both fields are not blank before saving
                                if (header.isBlank() || body.isBlank()) {
                                    // Could show a toast or snackbar here if desired
                                    return@IconButton
                                }
                                onSave(header, body) 
                            },
                            enabled = !isLoading && header.isNotBlank() && body.isNotBlank()
                        ) {
                            Icon(
                                Icons.Filled.Check, 
                                contentDescription = if (note.id < 0) "Create Note" else "Save Note"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (note.id < 0) "New Note" else "Last edited on ${formatDate(note.lastEdited)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .size(40.dp)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Error message
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            OutlinedTextField(
                shape = RoundedCornerShape(15.dp),
                value = header,
                onValueChange = {
                    header = it
                    onHeaderChange(it)
                },
                placeholder = { Text(if (note.id < 0) "Enter note title..." else "Note title") },
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth().padding(5.dp),
                enabled = !isLoading,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                shape = RoundedCornerShape(16.dp),
                value = body,
                onValueChange = {
                    body = it
                    onBodyChange(it)
                },
                placeholder = { Text(if (note.id < 0) "Write your note here..." else "Write your note here...") },
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(5.dp),
                enabled = !isLoading,
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = onDismissDelete,
                title = { 
                    Text(if (note.id < 0) "Discard this note?" else "Want to Delete this Note?") 
                },
                text = if (note.id < 0) {
                    { Text("This note hasn't been saved yet. Are you sure you want to discard it?") }
                } else null,
                confirmButton = {
                    TextButton(onClick = onConfirmDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(if (note.id < 0) "Discard" else "Delete Note", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissDelete) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel")
                        Spacer(Modifier.width(8.dp))
                        Text("Cancel", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    }
}

fun formatDate(millis: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(millis))
}

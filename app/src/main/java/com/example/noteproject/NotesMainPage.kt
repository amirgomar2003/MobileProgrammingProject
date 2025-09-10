package com.example.noteproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.noteproject.ui.components.Note
import com.example.noteproject.ui.PastelColors

@Composable
fun NotesMainPage(
    notes: List<Note>,
    onAddNote: () -> Unit,
    onNoteClick: (Note) -> Unit,
    onSettingsClick: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null,
    onLoadMore: (() -> Unit)? = null,
    hasNextPage: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    hasNotes: Boolean = false, // Whether user has any notes (for search bar visibility)
    isOfflineMode: Boolean = false,
    isSyncing: Boolean = false,
    onSyncClick: (() -> Unit)? = null
) {
    // Filter notes locally for immediate feedback, backend search will update the list
    val filteredNotes = if (searchQuery.isBlank()) {
        notes
    } else {
        notes.filter { 
            it.header.contains(searchQuery, ignoreCase = true) || 
            it.body.contains(searchQuery, ignoreCase = true) 
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                    NavigationBarItem(
                        selected = true,
                        onClick = { /* Already on home */ },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onSettingsClick,
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text("Settings") }
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0) // Remove default bottom padding
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // Offline/Sync Status Indicator
                if (isOfflineMode || isSyncing) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOfflineMode) {
                                MaterialTheme.colorScheme.tertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when {
                                    isSyncing -> Icons.Default.Sync
                                    isOfflineMode -> Icons.Default.CloudOff
                                    else -> Icons.Default.SyncProblem
                                },
                                contentDescription = null,
                                tint = if (isOfflineMode) {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = when {
                                    isSyncing -> "Syncing..."
                                    isOfflineMode -> "Offline mode - Changes will sync when online"
                                    else -> "Sync available"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isOfflineMode) {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                },
                                modifier = Modifier.weight(1f)
                            )
                            
                            if (isOfflineMode && !isSyncing && onSyncClick != null) {
                                TextButton(
                                    onClick = onSyncClick,
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text("Retry Sync")
                                }
                            }
                        }
                    }
                }
                
                // Show search bar if user has any notes (not just current search results)
                if (hasNotes || searchQuery.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search titles and content...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        }
                    )
                }
                
                // Error message
                errorMessage?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (onRetry != null) {
                                Spacer(Modifier.height(8.dp))
                                TextButton(onClick = onRetry) {
                                    Text("Retry", color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }
                    }
                }
                
                // Header section with conditional refresh button
                if (onRefresh != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Notes",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.padding(vertical = 20.dp)
                        ) {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = "Refresh Notes",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    Text(
                        "Notes",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                    )
                }
                
                // Show content based on state
                when {
                    // Show empty state only when user has no notes at all
                    !hasNotes -> {
                        Spacer(Modifier.height(20.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 80.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.jurney),
                                contentDescription = "Start Your Journey",
                                modifier = Modifier.size(180.dp)
                            )
                            Spacer(Modifier.height(24.dp))
                            Text(
                                "Start Your Journey",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Every big step start with small step.\nNotes your first idea and start your journey!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    // Show no search results message when search returns empty
                    searchQuery.isNotBlank() && filteredNotes.isEmpty() -> {
                        Spacer(Modifier.height(20.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 80.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "No notes found",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Try searching with different keywords",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    // Show notes grid
                    else -> {
                    Spacer(Modifier.height(20.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredNotes) { note ->
                            NoteCard(
                                note = note,
                                onClick = { onNoteClick(note) }
                            )
                        }
                        
                        // Add loading indicator at the end if loading more
                        if (isLoading && filteredNotes.isNotEmpty()) {
                            items(1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        
                        // Add load more button if there are more pages
                        if (hasNextPage && !isLoading && onLoadMore != null) {
                            items(1) {
                                OutlinedButton(
                                    onClick = onLoadMore,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text("Load More")
                                }
                            }
                        }
                    }
                }
            }
        }
        } // Close when statement

        // Show loading indicator when first loading
        if (isLoading && filteredNotes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        FloatingActionButton(
            onClick = onAddNote,
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Note")
        }
    }
}


@Composable
fun NoteCard(note: Note, onClick: () -> Unit) {
    val colorIndex = kotlin.math.abs(note.id % PastelColors.size)
    val bgColor = PastelColors[colorIndex].copy(alpha = 0.5f)

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                note.header,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                note.body,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

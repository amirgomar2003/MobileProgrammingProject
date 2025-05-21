package com.example.noteproject.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.noteproject.ui.Background
import com.example.noteproject.ui.PurpleDark
import com.example.noteproject.ui.PurplePrimary
import com.example.noteproject.ui.White

@Composable
fun NoteProjectTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = PurplePrimary,
            onPrimary = White,
            background = Background,
            onBackground = PurpleDark,
            surface = Background,
            onSurface = PurpleDark
        ),
        typography = Typography(),
        content = content
    )
}

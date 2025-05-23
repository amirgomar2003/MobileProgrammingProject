package com.example.noteproject.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ButtonWithArrow(config: ButtonWithArrowConfig) {
    val background = config.backgroundColor ?: MaterialTheme.colorScheme.primary
    val textColor = config.textColor ?: MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Button(
            onClick = config.onClick,
            enabled = config.enabled,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = background),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(Modifier.fillMaxSize()) {
                Text(
                    config.label,
                    modifier = Modifier.align(Alignment.Center),
                    color = textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                    tint = textColor
                )
            }
        }
    }
}

data class ButtonWithArrowConfig(
    val label: String,
    val backgroundColor: Color,
    val textColor: Color,
    val onClick: () -> Unit,
    val enabled: Boolean = true
    )
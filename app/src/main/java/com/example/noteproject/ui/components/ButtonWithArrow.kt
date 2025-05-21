package com.example.noteproject.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteproject.ui.GrayText

@Composable
fun ButtonWithArrow(config: ButtonWithArrowConfig) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Button(
            onClick = config.onClick,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = config.backgroundColor),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(Modifier.fillMaxSize()) {
                Text(
                    config.label,
                    modifier = Modifier.align(Alignment.Center),
                    color = config.textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                    tint = config.textColor
                )
            }
        }
    }
}

data class ButtonWithArrowConfig(
    val label: String,
    val backgroundColor: Color,
    val textColor: Color,
    val onClick: () -> Unit
)
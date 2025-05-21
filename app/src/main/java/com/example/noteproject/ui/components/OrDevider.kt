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
fun OrDevider(){
    // Or Divider.
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Divider(
            color = Color(0xFFE0E0E0),
            thickness = 1.dp,
            modifier = Modifier.weight(1f)
        )
        Text(
            "  Or  ",
            color = GrayText,
            fontSize = 14.sp
        )
        Divider(
            color = Color(0xFFE0E0E0),
            thickness = 1.dp,
            modifier = Modifier.weight(1f)
        )
    }
}
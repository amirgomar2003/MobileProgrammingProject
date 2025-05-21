package com.example.noteproject

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.noteproject.ui.*
import com.example.noteproject.ui.components.ButtonWithArrow
import com.example.noteproject.ui.components.ButtonWithArrowConfig

@Composable
fun OnboardingScreen(
    onGetStartedClick: () -> Unit
) {
    val OnboardingButtonConfig = ButtonWithArrowConfig(
        label = "Let's Get Started",
        backgroundColor = MaterialTheme.colorScheme.background,
        textColor = MaterialTheme.colorScheme.onBackground,
        onClick = onGetStartedClick
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PurplePrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Image(
                painter = painterResource(id = R.drawable.onboarding),
                contentDescription = "Onboarding",
                modifier = Modifier.size(300.dp)
            )
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Jot Down anything you want to achieve, today or in the future",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
            ButtonWithArrow(OnboardingButtonConfig)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
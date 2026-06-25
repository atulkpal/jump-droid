package com.example.jump_droid

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jump_droid.ui.theme.SciFiCyan
import com.example.jump_droid.ui.theme.SciFiGold
import com.example.jump_droid.ui.theme.SciFiWhite

@Composable
fun AscensionOverlay(
    onComplete: () -> Unit
) {
    val scrollState = rememberScrollState()
    val totalCreditsTime = 30000 // 30 seconds
    
    LaunchedEffect(Unit) {
        scrollState.animateScrollTo(
            value = 5000, // Large enough to scroll past everything
            animationSpec = tween(durationMillis = totalCreditsTime, easing = LinearEasing)
        )
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Blinding white for the Singularity
            .verticalScroll(scrollState)
            .padding(vertical = 300.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.widthIn(max = 600.dp)
        ) {
            Text(
                text = "THE FINAL TRUTH",
                style = MaterialTheme.typography.displayMedium,
                color = Color.Black,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp
            )
            
            Spacer(Modifier.height(100.dp))
            
            LoreSection(
                title = "THE ARCHITECT'S LOG",
                content = "The Ascension Program was never a search for answers. It was a homing beacon. We built the Sky to keep you safe, but we also built the Path to bring you back. You have proven your adaptability, your resilience, and your will to ascend."
            )
            
            Spacer(Modifier.height(200.dp))
            
            Text(
                text = "HALL OF PIONEERS",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(40.dp))
            
            CreditRow("Lead Engineer", "Atul")
            CreditRow("AI Architect", "Architecture Agent")
            CreditRow("Systems Logic", "EPIC Framework")
            CreditRow("Visual Fidelity", "Compose Canvas")
            
            Spacer(Modifier.height(400.dp))
            
            Text(
                text = "YOU HAVE REACHED POINT ZERO",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                letterSpacing = 4.sp
            )
        }
    }
}

@Composable
private fun LoreSection(title: String, content: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            content,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp,
            fontSize = 18.sp
        )
    }
}

@Composable
private fun CreditRow(role: String, name: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(role, color = Color.Gray, fontWeight = FontWeight.Light)
        Spacer(Modifier.width(16.dp))
        Text(name, color = Color.Black, fontWeight = FontWeight.Bold)
    }
}

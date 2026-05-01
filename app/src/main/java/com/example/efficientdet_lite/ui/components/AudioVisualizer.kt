package com.example.efficientdet_lite.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.efficientdet_lite.announcements.AnnouncementType
import com.example.efficientdet_lite.audio.AudioDebugViewModel

@Composable
fun AudioVisualizer(viewModel: AudioDebugViewModel) {
    val animatedAmplitude by animateFloatAsState(targetValue = viewModel.amplitude)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Whisper AI Test", style = MaterialTheme.typography.titleMedium)
        
        Spacer(modifier = Modifier.height(8.dp))

        // --- ACTIVE ALERT CARD ---
        viewModel.activeAlert?.let { alert ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (alert.type) {
                        AnnouncementType.FINAL_CALL -> Color(0xFFFFEBEE)
                        AnnouncementType.GATE_CHANGE -> Color(0xFFE3F2FD)
                        else -> Color(0xFFF1F8E9)
                    }
                ),
                border = BorderStroke(2.dp, when (alert.type) {
                    AnnouncementType.FINAL_CALL -> Color.Red
                    AnnouncementType.GATE_CHANGE -> Color(0xFF2196F3)
                    else -> Color(0xFF4CAF50)
                }),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "✈️ MATCH FOUND",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = alert.type.name.replace("_", " "),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Flight ${alert.matchedFlightNumber}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    
                    alert.gate?.let { gate ->
                        Text(
                            text = "Gate $gate",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF1F6BFF),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (alert.type == AnnouncementType.DELAY && alert.delayText != null) {
                        Text(
                            text = "Status: Delayed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFD32F2F)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.clearAlert() },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("Dismiss", color = Color.White)
                    }
                }
            }
        }

        // Transcription Result Box
        Surface(
            color = Color.Black.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)
        ) {
            Text(
                text = viewModel.transcription,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Visual bar representing amplitude
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(Color.LightGray, RoundedCornerShape(6.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedAmplitude.coerceIn(0.01f, 1f))
                    .fillMaxHeight()
                    .background(
                        if (animatedAmplitude > 0.8f) Color.Red else Color.Green,
                        RoundedCornerShape(6.dp)
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (!viewModel.isAILoaded) {
            Button(
                onClick = { viewModel.loadAI() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F6BFF))
            ) {
                Text("Load Whisper AI (660MB)")
            }
        } else {
            Button(onClick = { viewModel.toggleRecording() }) {
                Text(if (viewModel.isRecording) "Stop Listening" else "Start Whisper Test")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = { viewModel.runAssetTest("aa123_boarding.wav") }) {
            Text("Test: AA123 Boarding")
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        OutlinedButton(onClick = { viewModel.runAssetTest("aa123_gate_change.wav") }) {
            Text("Test: AA123 Gate Change")
        }

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedButton(onClick = { viewModel.runAssetTest("aa123_delay.wav") }) {
            Text("Test: AA123 Delay")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(onClick = { viewModel.runLogicTest() }) {
            Text("Run Logic Test (Text Only)")
        }
    }
}

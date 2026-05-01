package com.example.efficientdet_lite.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

        OutlinedButton(onClick = { viewModel.runAssetTest() }) {
            Text("Run Asset (.wav) Test")
        }
    }
}

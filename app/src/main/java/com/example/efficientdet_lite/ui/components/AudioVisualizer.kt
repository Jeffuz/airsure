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
        Text(text = "Audio Debugger", style = MaterialTheme.typography.titleMedium)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Visual bar representing amplitude
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .background(Color.LightGray, RoundedCornerShape(10.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedAmplitude.coerceIn(0.01f, 1f))
                    .fillMaxHeight()
                    .background(
                        if (animatedAmplitude > 0.8f) Color.Red else Color.Green,
                        RoundedCornerShape(10.dp)
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = { viewModel.toggleRecording() }) {
            Text(if (viewModel.isRecording) "Stop Mic Test" else "Start Mic Test")
        }
        
        if (viewModel.isRecording) {
            Text(
                text = "Amplitude: ${(animatedAmplitude * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

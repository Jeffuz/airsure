package com.example.efficientdet_lite.audio

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.efficientdet_lite.announcements.FlightViewModel
import com.example.efficientdet_lite.ui.components.AudioVisualizer

@Composable
fun AudioDebugScreen(flightViewModel: FlightViewModel) {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    
    var hasAudioPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasAudioPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasAudioPermission) {
            val audioViewModel: AudioDebugViewModel = viewModel(
                factory = AudioDebugViewModel.Factory(application, flightViewModel)
            )
            LaunchedEffect(audioViewModel) {
                audioViewModel.loadAI()
                audioViewModel.toggleRecording()
            }

            Surface(
                color = Color.White.copy(alpha = 0.9f),
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
                    .width(300.dp)
            ) {
                AudioVisualizer(viewModel = audioViewModel)
            }
        } else {
            Text(
                text = "Audio permission is required for testing",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

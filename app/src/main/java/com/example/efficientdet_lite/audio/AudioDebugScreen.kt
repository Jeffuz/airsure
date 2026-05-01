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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.efficientdet_lite.R
import com.example.efficientdet_lite.ui.theme.AirSureBg
import com.example.efficientdet_lite.ui.theme.AirSureBlue
import com.example.efficientdet_lite.ui.theme.AirSurePrimary
import com.example.efficientdet_lite.ui.theme.AirSureProgressTrack
import com.example.efficientdet_lite.ui.theme.AirSureSubtitle

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
            }

            when {
                audioViewModel.isLoadingAI || (!audioViewModel.isAILoaded && audioViewModel.loadError == null) -> {
                    WhisperLoadingScreen(
                        statusText = audioViewModel.loadingMessage,
                        progress = audioViewModel.loadingProgress
                    )
                }

                audioViewModel.loadError != null -> {
                    Text(
                        text = "Failed to load Whisper AI: ${audioViewModel.loadError}",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
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
                }
            }
        }
    }
}

@Composable
private fun WhisperLoadingScreen(
    statusText: String,
    progress: Float
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AirSureBg)
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_airport_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 130.dp)
                .alpha(0.55f),
            contentScale = ContentScale.FillWidth
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(220.dp))

            Image(
                painter = painterResource(id = R.drawable.airsure_logo),
                contentDescription = "AirSure logo",
                modifier = Modifier.size(88.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "AirSure",
                color = AirSurePrimary,
                fontSize = 46.sp,
                lineHeight = 52.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Preparing local announcement AI",
                color = AirSureSubtitle,
                fontSize = 17.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif
            )

            Text(
                text = "This may take a moment the first time.",
                color = AirSureSubtitle,
                fontSize = 17.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = statusText,
                color = AirSureBlue,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(26.dp))

            Box(
                modifier = Modifier
                    .width(172.dp)
                    .height(4.dp)
                    .background(
                        color = AirSureProgressTrack,
                        shape = RoundedCornerShape(999.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(4.dp)
                        .background(
                            color = AirSureBlue,
                            shape = RoundedCornerShape(999.dp)
                        )
                )
            }

            Spacer(
                modifier = Modifier
                    .navigationBarsPadding()
                    .height(28.dp)
            )
        }
    }
}

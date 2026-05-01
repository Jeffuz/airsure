package com.example.efficientdet_lite.audio

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.efficientdet_lite.R
import com.example.efficientdet_lite.ui.components.AudioVisualizer
import com.example.efficientdet_lite.ui.theme.AirSureBg
import com.example.efficientdet_lite.ui.theme.AirSureBlue
import com.example.efficientdet_lite.ui.theme.AirSurePrimary
import com.example.efficientdet_lite.ui.theme.AirSureProgressTrack
import com.example.efficientdet_lite.ui.theme.AirSureSubtitle

@Composable
fun AudioDebugScreen(viewModel: AudioDebugViewModel, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        LaunchedEffect(viewModel) {
            viewModel.loadAI()
        }

        LaunchedEffect(viewModel.isAILoaded) {
            if (viewModel.isAILoaded) {
                viewModel.startListeningIfReady()
            }
        }

        DisposableEffect(viewModel) {
            onDispose {
                viewModel.stopListening()
            }
        }

        when {
            viewModel.isLoadingAI || (!viewModel.isAILoaded && viewModel.loadError == null) -> {
                WhisperLoadingScreen(
                    statusText = viewModel.loadingMessage,
                    progress = viewModel.loadingProgress
                )
            }

            viewModel.loadError != null -> {
                Text(
                    text = "Failed to load Whisper AI: ${viewModel.loadError}",
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                AudioVisualizer(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
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
                    .height(28.dp)
            )
        }
    }
}

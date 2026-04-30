package com.example.efficientdet_lite.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.efficientdet_lite.ui.theme.AirSureBg
import com.example.efficientdet_lite.ui.theme.AirSureBlue
import com.example.efficientdet_lite.ui.theme.AirSureDivider
import com.example.efficientdet_lite.ui.theme.AirSurePrimary
import com.example.efficientdet_lite.ui.theme.AirSureProgressTrack
import com.example.efficientdet_lite.ui.theme.AirSureSubtitle
import kotlinx.coroutines.delay

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.remember

import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    val splashDurationMs = 2000
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = splashDurationMs,
                easing = LinearEasing
            )
        )

        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AirSureBg)
    ) {
        // Faded airport background image near bottom
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

        // Main centered content
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
                text = "Your on-device airport assistant.",
                color = AirSureSubtitle,
                fontSize = 17.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif
            )

            Text(
                text = "Private. Offline. Always ready.",
                color = AirSureSubtitle,
                fontSize = 17.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bottom trust row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
//                Box(
//                    modifier = Modifier
//                        .width(52.dp)
//                        .height(1.dp)
//                        .background(AirSureDivider)
//                )

                Spacer(modifier = Modifier.width(12.dp))

//                Text(
//                    text = "🔒",
//                    fontSize = 20.sp
//                )

                Spacer(modifier = Modifier.width(12.dp))

//                Box(
//                    modifier = Modifier
//                        .width(52.dp)
//                        .height(1.dp)
//                        .background(AirSureDivider)
//                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Runs locally · No Wi-Fi required",
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
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.value)
                        .height(4.dp)
                        .background(
                            color = AirSureBlue,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp)
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

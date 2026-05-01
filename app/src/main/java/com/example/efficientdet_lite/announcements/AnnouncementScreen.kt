package com.example.efficientdet_lite.announcements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.example.efficientdet_lite.vision.EfficientDetCameraScreen
import com.example.efficientdet_lite.audio.AudioDebugScreen


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CarryOnScannerScreen(
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = onBackClick) {
            Text("Back")
        }

        EfficientDetCameraScreen()
    }
}

@Composable
fun AnnouncementScreen(
    onBackClick: () -> Unit,
    flightViewModel: FlightViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF8FBFF),
                        Color.White
                    )
                )
            )
    ) {
        AudioDebugScreen(
            flightViewModel = flightViewModel
        )

        AnnouncementTopBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 18.dp)
                .padding(top = 14.dp),
            onBackClick = onBackClick
        )
    }
}

@Composable
private fun AnnouncementTopBar(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.97f))
                .border(
                    width = 1.dp,
                    color = Color(0xFFE0E7F5),
                    shape = RoundedCornerShape(999.dp)
                )
                .clickable { onBackClick() }
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF1F6BFF),
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(7.dp))

            Text(
                text = "Back",
                color = Color(0xFF1F6BFF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.97f))
                .border(
                    width = 1.dp,
                    color = Color(0xFFE0E7F5),
                    shape = RoundedCornerShape(999.dp)
                )
                .padding(horizontal = 13.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEAF3FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = null,
                    tint = Color(0xFF1F6BFF),
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(7.dp))

            Text(
                text = "Listening locally",
                color = Color(0xFF1F6BFF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

package com.example.efficientdet_lite.announcements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.example.efficientdet_lite.vision.EfficientDetCameraScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.efficientdet_lite.app.TripDetails


@Composable
fun AnnouncementScreen(
    onBackClick: () -> Unit,
    tripDetails: TripDetails,
    onBoardingPassClick: () -> Unit
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
        if (tripDetails.flight.isBlank()) {
            MissingFlightCodeState(
                modifier = Modifier.align(Alignment.Center),
                onBoardingPassClick = onBoardingPassClick
            )
        } else {
            AudioDebugScreen(
                tripDetails = tripDetails
            )
        }

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
private fun MissingFlightCodeState(
    modifier: Modifier = Modifier,
    onBoardingPassClick: () -> Unit
) {
    Column(
        modifier = modifier
            .padding(horizontal = 28.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0xFFE0E7F5),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(Color(0xFFEAF3FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Mic,
                contentDescription = null,
                tint = Color(0xFF1F6BFF),
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.padding(top = 18.dp))

        Text(
            text = "Flight code required",
            color = Color(0xFF13235E),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.padding(top = 8.dp))

        Text(
            text = "Enter your boarding pass information before using Flight Alerts. Only the flight code is required.",
            color = Color(0xFF6D7898),
            fontSize = 15.sp,
            lineHeight = 21.sp
        )

        Spacer(modifier = Modifier.padding(top = 22.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xFF1F6BFF))
                .clickable { onBoardingPassClick() }
                .padding(horizontal = 22.dp, vertical = 13.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Enter boarding pass",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
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

package com.example.efficientdet_lite.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.efficientdet_lite.announcements.AnnouncementType
import com.example.efficientdet_lite.announcements.FlightAnnouncement
import com.example.efficientdet_lite.audio.AudioDebugViewModel

private val Navy = Color(0xFF13235E)
private val BodyGray = Color(0xFF6D7898)
private val MutedGray = Color(0xFF7D87A2)
private val Blue = Color(0xFF1F6BFF)
private val LightBlue = Color(0xFFEAF3FF)
private val BorderBlue = Color(0xFFCFE0FF)
private val Green = Color(0xFF14866D)
private val Orange = Color(0xFFE8892F)
private val Red = Color(0xFFE54848)

@Composable
fun AudioVisualizer(
    viewModel: AudioDebugViewModel,
    modifier: Modifier = Modifier,
) {
    val animatedAmplitude by animateFloatAsState(targetValue = viewModel.amplitude)

    val isActualTranscription = viewModel.transcription.isNotBlank() &&
            !viewModel.transcription.contains("Listening", ignoreCase = true) &&
            !viewModel.transcription.contains("Transcribing", ignoreCase = true) &&
            !viewModel.transcription.contains("AI is still loading", ignoreCase = true) &&
            !viewModel.transcription.contains("(No speech detected)", ignoreCase = true)

    val statusTitle = when {
        viewModel.transcription.contains("Transcribing", ignoreCase = true) -> "Transcribing announcement"
        viewModel.isRecording -> "Listening for airport updates"
        viewModel.isAILoaded -> "Ready to listen"
        else -> "Preparing local AI"
    }

    val statusSubtitle = when {
        viewModel.isRecording -> "AirSure is listening locally for your saved flight code."
        viewModel.isAILoaded -> "Starting local listener..."
        else -> "Loading the on-device speech model."
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
            .padding(top = 12.dp, bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Flight Alerts",
            color = Navy,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.6).sp,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Listen for boarding calls, gate changes, and delay updates in real time.",
            color = BodyGray,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(22.dp))

        StatusCard(
            title = statusTitle,
            subtitle = statusSubtitle,
            isRecording = viewModel.isRecording,
            amplitude = animatedAmplitude
        )

        if (isActualTranscription) {
            Spacer(modifier = Modifier.height(16.dp))

            TranscriptionCard(
                text = viewModel.transcription
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        viewModel.activeAlert?.let { alert ->
            AlertCard(
                alert = alert
            ) { viewModel.clearAlert() }
        }
    }
}

@Composable
private fun TranscriptionCard(
    text: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, Blue.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = LightBlue.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Blue)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "LIVE",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Announced locally",
                    color = Blue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = text,
                color = Navy,
                fontSize = 17.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    subtitle: String,
    isRecording: Boolean,
    amplitude: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, BorderBlue),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            LightBlue
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Mic,
                        contentDescription = null,
                        tint = Blue,
                        modifier = Modifier.size(25.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        color = Navy,
                        fontSize = 18.sp,
                        lineHeight = 23.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = subtitle,
                        color = BodyGray,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }

                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE54848).copy(alpha = amplitude.coerceIn(0.2f, 1f)))
                    )
                }
            }

        }
    }
}

@Composable
private fun AlertCard(
    alert: FlightAnnouncement,
    onDismiss: () -> Unit
) {
    val accentColor = when (alert.type) {
        AnnouncementType.GATE_CHANGE -> Blue
        AnnouncementType.DELAY -> Orange
        AnnouncementType.FINAL_CALL -> Red
        else -> Green
    }

    val title = when (alert.type) {
        AnnouncementType.GATE_CHANGE -> "Gate change detected"
        AnnouncementType.DELAY -> "Delay update detected"
        AnnouncementType.FINAL_CALL -> "Final call detected"
        else -> "Boarding announcement detected"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Flight,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(25.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        color = Navy,
                        fontSize = 18.sp,
                        lineHeight = 23.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Flight ${alert.matchedFlightNumber}",
                        color = accentColor,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                androidx.compose.material3.IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = BodyGray.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            alert.gate?.let { gate ->
                Spacer(modifier = Modifier.height(16.dp))

                InfoPill(
                    label = "Gate",
                    value = gate,
                    accentColor = accentColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = alert.rawTranscript,
                color = Color(0xFF4E5A78),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun InfoPill(
    label: String,
    value: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            color = MutedGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = value,
            color = accentColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

package com.example.efficientdet_lite.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
    modifier: Modifier = Modifier
) {
    val animatedAmplitude by animateFloatAsState(targetValue = viewModel.amplitude)

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
            .padding(top = 12.dp, bottom = 28.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Flight Alerts",
            color = Navy,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.6).sp,
            modifier = Modifier.fillMaxWidth()
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

        Spacer(modifier = Modifier.height(16.dp))

        viewModel.activeAlert?.let { alert ->
            AlertCard(
                alert = alert,
                onDismiss = { viewModel.clearAlert() }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        LatestAnnouncementCard(
            transcription = viewModel.transcription
        )

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedButton(
            onClick = { viewModel.runAssetTest("aa123_boarding.wav") },
            shape = RoundedCornerShape(999.dp),
            border = BorderStroke(1.dp, Color(0xFFCAD6EA)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Blue
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Test AA123 Boarding",
                fontSize = 15.sp,
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
                            if (isRecording) Color(0xFFEAF7F4) else LightBlue
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Mic,
                        contentDescription = null,
                        tint = if (isRecording) Green else Blue,
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
            }

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0xFFE5EAF3))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(
                            if (isRecording) amplitude.coerceIn(0.08f, 1f) else 0.08f
                        )
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    if (isRecording) Green else Blue,
                                    Blue
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
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

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = accentColor
                ),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "Dismiss",
                    fontWeight = FontWeight.Medium
                )
            }
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

@Composable
private fun LatestAnnouncementCard(
    transcription: String
) {
    val placeholderTexts = setOf(
        "Ready to listen.",
        "Listening...",
        "Listening again...",
        "Stopped.",
        "Loading Whisper AI..."
    )

    val displayText = if (
        transcription.isBlank() ||
        placeholderTexts.contains(transcription)
    ) {
        "No announcement captured yet."
    } else {
        transcription
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E7F5)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsNone,
                    contentDescription = null,
                    tint = Blue,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Latest announcement",
                    color = Navy,
                    fontSize = 18.sp,
                    lineHeight = 23.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 88.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFF6F8FC))
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = displayText,
                    color = if (displayText == "No announcement captured yet.") MutedGray else Color(0xFF34405F),
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}
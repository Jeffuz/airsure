package com.example.efficientdet_lite.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.efficientdet_lite.R
import com.example.efficientdet_lite.app.TripDetails

// icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector

private val Navy = Color(0xFF0D1B5E)
private val LogoNavy = Color(0xFF13235E)
private val BodyGray = Color(0xFF6D7898)
private val MutedGray = Color(0xFF7D87A2)
private val LabelGray = Color(0xFF8D96AE)
private val Blue = Color(0xFF1F6BFF)
private val LightBlue = Color(0xFFEAF3FF)
private val BorderBlue = Color(0xFFCFE0FF)
private val Green = Color(0xFF14866D)

@Composable
fun HomeScreen(
    tripDetails: TripDetails,
    onScanCarryOnClick: () -> Unit,
    onAnnouncementsClick: () -> Unit,
    onRecentSubmissionsClick: () -> Unit,
    onBoardingPassClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF8FBFF),
                        Color(0xFFFFFFFF)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(top = 24.dp, bottom = 92.dp)
        ) {
            HomeHeader()

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "Ready for your trip?",
                color = Navy,
                fontSize = 26.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.8).sp,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Scan bags and listen for airport updates before boarding.",
                color = BodyGray,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = (-0.1).sp,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(30.dp))

            BoardingPassCard(
                tripDetails = tripDetails,
                onClick = onBoardingPassClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureCard(
                    modifier = Modifier.weight(1f),
                    title = "Scan Carry-On",
                    subtitle = "Check restricted or\nrisky items",
                    icon = Icons.Outlined.Luggage,
                    accentColor = Blue,
                    background = Brush.linearGradient(
                        listOf(Color(0xFFF1F7FF), Color(0xFFEAF3FF))
                    ),
                    onClick = onScanCarryOnClick
                )

                FeatureCard(
                    modifier = Modifier.weight(1f),
                    title = "Flight Alerts",
                    subtitle = "Detect gate changes and boarding calls",
                    icon = Icons.Outlined.Mic,
                    accentColor = Color(0xFF008C7A),
                    background = Brush.linearGradient(
                        listOf(Color(0xFFF2FBF8), Color(0xFFEAF7F4))
                    ),
                    onClick = onAnnouncementsClick
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            RecentSubmissionsCard(onClick = onRecentSubmissionsClick)
        }

        BottomNavBar(
            activeRoute = "home",
            modifier = Modifier.align(Alignment.BottomCenter),
            onHomeClick = { },
            onScanClick = onScanCarryOnClick,
            onListenClick = onAnnouncementsClick
        )
    }
}

@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.airsure_logo),
                contentDescription = "AirSure logo",
                modifier = Modifier.size(34.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "AirSure",
                color = LogoNavy,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.88f))
                .border(
                    width = 1.dp,
                    color = Color(0xFFE2E8F4),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Blue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "On-device ready",
                color = Green,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BoardingPassCard(
    tripDetails: TripDetails,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, BorderBlue),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF4FAFF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, Blue, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ConfirmationNumber,
                        contentDescription = null,
                        tint = Blue,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Boarding Pass",
                        color = Color(0xFF16245F),
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.3).sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Add your trip to get personalized alerts.",
                        color = Color(0xFF6F7A99),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, BorderBlue, RoundedCornerShape(14.dp))
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add / Scan",
                        color = Blue,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.1).sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "›",
                    color = Color(0xFF5F6B8A),
                    fontSize = 30.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TravelInfoColumn(
                    label = "FROM",
                    value = tripDetails.from.ifBlank { "—" },
                    subtitle = if (tripDetails.from.isBlank()) "Not set" else "Origin",
                    modifier = Modifier.weight(0.8f)
                )

                DividerLine()

                TravelInfoColumn(
                    label = "TO",
                    value = tripDetails.to.ifBlank { "—" },
                    subtitle = if (tripDetails.to.isBlank()) "Not set" else "Destination",
                    modifier = Modifier.weight(0.8f)
                )

                DividerLine()

                TravelInfoColumn(
                    label = "DATE",
                    value = tripDetails.date.ifBlank { "—" },
                    subtitle = if (tripDetails.date.isBlank()) "Not set" else "Trip date",
                    modifier = Modifier.weight(0.8f)
                )

                DividerLine()

                TravelInfoColumn(
                    label = "FLIGHT",
                    value = tripDetails.flight.ifBlank { "—" },
                    subtitle = if (tripDetails.flight.isBlank()) "Not set" else "Flight",
                    modifier = Modifier.weight(0.8f)
                )

                DividerLine()

                TravelInfoColumn(
                    label = "GATE",
                    value = tripDetails.gate.ifBlank { "—" },
                    subtitle = if (tripDetails.gate.isBlank()) "Pending" else "Gate",
                    modifier = Modifier.weight(0.8f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEAF4FF))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = Blue,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Runs locally · No Wi-Fi required",
                    color = Blue,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.1).sp
                )
            }
        }
    }
}

@Composable
private fun TravelInfoColumn(
    label: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 6.dp),
        verticalArrangement = Arrangement.Center
    ) {
        val MutedGray = Color(0xFF7D87A2)
        val LabelGray = Color(0xFF8D96AE)
        Text(
            text = label,
            color = LabelGray,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.4.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            color = Color(0xFF16245F),
            fontSize = 16.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.2).sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = subtitle,
            color = MutedGray,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(58.dp)
            .background(Color(0xFFD8E3F2))
    )
}

@Composable
private fun FeatureCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    background: Brush,
    onClick: () -> Unit
) {
    val BorderBlue = Color(0xFFCFE0FF)
    val LogoNavy = Color(0xFF13235E)
    Card(
        modifier = modifier
            .height(194.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, BorderBlue),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(38.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = title,
                    color = LogoNavy,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = subtitle,
                    color = Color(0xFF667395),
                    fontSize = 15.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, BorderBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun RecentSubmissionsCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsNone,
                    contentDescription = null,
                    tint = Color(0xFF17245D),
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Recent submissions",
                    color = Color(0xFF17245D),
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "View list  ›",
                    color = Color(0xFF2A71FF),
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "View items you've previously scanned for carry-on compliance.",
                color = Color(0xFF6D7898),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun AlertRow(
    icon: String,
    iconBg: Color,
    badge: String?,
    time: String,
    headline: String,
    body: String,
    meta: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 22.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val Red = Color(0xFFFF5A4F)
                if (badge != null) {
                    Text(
                        text = badge,
                        color = Red,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = time,
                    color = Color(0xFF8C94AA),
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = Color(0xFF17245D),
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(headline)
                    }

                    withStyle(
                        SpanStyle(
                            color = Color(0xFF17245D),
                            fontWeight = FontWeight.Normal
                        )
                    ) {
                        append(body)
                    }
                },
                fontSize = 15.sp,
                lineHeight = 21.sp,
                letterSpacing = (-0.1).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = meta,
                color = MutedGray,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "›",
            color = Color(0xFF6B7592),
            fontSize = 30.sp
        )
    }
}

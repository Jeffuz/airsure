package com.example.efficientdet_lite.carryon

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import com.example.efficientdet_lite.ui.BottomNavBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qualcomm.qti.objectdetection.RestrictionManager

private val Navy = Color(0xFF0D1B5E)
private val BodyGray = Color(0xFF667395)
private val MutedGray = Color(0xFF7D87A2)
private val Blue = Color(0xFF1F6BFF)
private val BorderBlue = Color(0xFFD7E6FF)

private val Red = Color(0xFFE52520)
private val RedBg = Color(0xFFFFF3F2)
private val RedBorder = Color(0xFFFFD7D3)

private val Orange = Color(0xFFFF8A00)
private val OrangeBg = Color(0xFFFFF8EC)
private val OrangeBorder = Color(0xFFFFE4BE)

private val Green = Color(0xFF18B872)
private val GreenBg = Color(0xFFEFFBF6)
private val GreenBorder = Color(0xFFD4F3E5)

@Composable
fun ItemDetailsScreen(
    items: List<Pair<String, RestrictionManager.TravelInfo?>>,
    onBackClick: () -> Unit,
    onAddAnotherClick: () -> Unit,
    onHomeClick: () -> Unit,
    onListenClick: () -> Unit
) {
    val attentionCount = items.count { (_, info) ->
        info?.level == RestrictionManager.Level.DANGER ||
                info?.level == RestrictionManager.Level.CAUTION
    }

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
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                BottomNavBar(
                    activeRoute = "scan",
                    onHomeClick = onHomeClick,
                    onScanClick = onAddAnotherClick,
                    onListenClick = onListenClick
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp)
                    .padding(top = 18.dp, bottom = 28.dp)
            ) {
                ResultHeader(
                    onBackClick = onBackClick
                )

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = "Carry-On Scan Complete",
                    color = Navy,
                    fontSize = 30.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.8).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (attentionCount == 1) {
                        "1 item needs attention before security."
                    } else {
                        "$attentionCount items need attention before security."
                    },
                    color = BodyGray,
                    fontSize = 18.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (items.isEmpty()) {
                    EmptyScanCard()
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items.forEach { (label, info) ->
                            ScanResultCard(
                                label = label,
                                info = info
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onAddAnotherClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Scan again",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onHomeClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Blue
                    ),
                    border = BorderStroke(1.dp, Color(0xFFE0E7F5)),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "Back home",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = null,
                        tint = Color(0xFF8C94AA),
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Runs locally · No Wi-Fi required",
                        color = Color(0xFF8C94AA),
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultHeader(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White)
                .border(
                    width = 1.dp,
                    color = Color(0xFFE0E7F5),
                    shape = RoundedCornerShape(999.dp)
                )
                .clickable { onBackClick() }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = Navy,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Back",
                color = Navy,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White)
                .border(
                    width = 1.dp,
                    color = Color(0xFFE0E7F5),
                    shape = RoundedCornerShape(999.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Blue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Scan complete",
                color = Color(0xFF0D766E),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EmptyScanCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, BorderBlue),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp)
        ) {
            Text(
                text = "No items detected",
                color = Navy,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Try scanning your carry-on again.",
                color = BodyGray,
                fontSize = 15.sp,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun ScanResultCard(
    label: String,
    info: RestrictionManager.TravelInfo?
) {
    val visual = resultVisual(info)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, visual.borderColor),
        colors = CardDefaults.cardColors(
            containerColor = visual.backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(visual.iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = null,
                    tint = visual.accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = cleanLabel(label),
                    color = Navy,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = visual.accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(visual.primaryMessage)
                        }

                        append("\n")

                        withStyle(
                            SpanStyle(
                                color = BodyGray,
                                fontWeight = FontWeight.Normal
                            )
                        ) {
                            append(visual.secondaryMessage)
                        }
                    },
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                )
            }
        }
    }
}

private data class ResultVisual(
    val accentColor: Color,
    val backgroundColor: Color,
    val borderColor: Color,
    val iconBackground: Color,
    val icon: ImageVector,
    val primaryMessage: String,
    val secondaryMessage: String
)

private fun resultVisual(
    info: RestrictionManager.TravelInfo?
): ResultVisual {
    return when (info?.level) {
        RestrictionManager.Level.DANGER -> {
            ResultVisual(
                accentColor = Red,
                backgroundColor = RedBg,
                borderColor = RedBorder,
                iconBackground = Color(0xFFFFDFDC),
                icon = Icons.Outlined.WarningAmber,
                primaryMessage = "Not allowed in carry-on",
                secondaryMessage = info.message.ifBlank {
                    "Prohibited item. Pack in checked baggage."
                }
            )
        }

        RestrictionManager.Level.CAUTION -> {
            ResultVisual(
                accentColor = Orange,
                backgroundColor = OrangeBg,
                borderColor = OrangeBorder,
                iconBackground = Color(0xFFFFEED8),
                icon = Icons.Outlined.WarningAmber,
                primaryMessage = "Careful",
                secondaryMessage = info.message.ifBlank {
                    "TSA 3-1-1 Rule applies. Check container size."
                }
            )
        }

        RestrictionManager.Level.INFO -> {
            ResultVisual(
                accentColor = Green,
                backgroundColor = GreenBg,
                borderColor = GreenBorder,
                iconBackground = Color(0xFFDDF7EA),
                icon = Icons.Outlined.Check,
                primaryMessage = "Allowed",
                secondaryMessage = info.message.ifBlank {
                    "Standard personal item."
                }
            )
        }

        else -> {
            ResultVisual(
                accentColor = MutedGray,
                backgroundColor = Color(0xFFF8FAFD),
                borderColor = Color(0xFFE1E7F0),
                iconBackground = Color(0xFFEFF3F8),
                icon = Icons.Outlined.WarningAmber,
                primaryMessage = "Review item",
                secondaryMessage = "No restriction details available."
            )
        }
    }
}

private fun cleanLabel(label: String): String {
    return label
        .replace("_", " ")
        .replace("-", " ")
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
}

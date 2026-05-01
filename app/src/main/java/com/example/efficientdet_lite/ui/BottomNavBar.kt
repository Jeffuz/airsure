package com.example.efficientdet_lite.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Blue = Color(0xFF1F6BFF)

@Composable
fun BottomNavBar(
    activeRoute: String,
    onHomeClick: () -> Unit,
    onScanClick: () -> Unit,
    onListenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFE5EAF3))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 34.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavIndicator(active = activeRoute == "home", modifier = Modifier.weight(1f))
            BottomNavIndicator(active = activeRoute == "scan", modifier = Modifier.weight(1f))
            BottomNavIndicator(active = activeRoute == "listen", modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 34.dp)
                .padding(top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                modifier = Modifier.weight(1f),
                label = "Home",
                icon = Icons.Outlined.Home,
                active = activeRoute == "home",
                onClick = onHomeClick
            )

            BottomNavItem(
                modifier = Modifier.weight(1f),
                label = "Scan",
                icon = Icons.Outlined.QrCodeScanner,
                active = activeRoute == "scan",
                onClick = onScanClick
            )

            BottomNavItem(
                modifier = Modifier.weight(1f),
                label = "Listen",
                icon = Icons.Outlined.Mic,
                active = activeRoute == "listen",
                onClick = onListenClick
            )
        }
    }
}

@Composable
private fun BottomNavIndicator(
    active: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(46.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(if (active) Blue else Color.Transparent)
        )
    }
}

@Composable
private fun BottomNavItem(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit
) {
    val itemColor = if (active) Blue else Color(0xFF7F88A2)

    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = itemColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            color = itemColor,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = if (active) FontWeight.Medium else FontWeight.Normal
        )
    }
}

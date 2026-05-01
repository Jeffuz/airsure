package com.example.efficientdet_lite.carryon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.efficientdet_lite.ui.BottomNavBar
import com.qualcomm.qti.objectdetection.RestrictionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    items: List<Pair<String, RestrictionManager.TravelInfo?>>,
    onBackClick: () -> Unit,
    onAddAnotherClick: () -> Unit,
    onHomeClick: () -> Unit,
    onListenClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detected Items", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8FAFD)
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                activeRoute = "scan",
                onHomeClick = onHomeClick,
                onScanClick = { /* Already here */ },
                onListenClick = onListenClick
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddAnotherClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Another") },
                containerColor = Color(0xFF1F6BFF),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        },
        containerColor = Color(0xFFF8FAFD)
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No items detected.", color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onAddAnotherClick) {
                        Text("Start Scanning")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { (label, info) ->
                    ItemRow(label, info)
                }
            }
        }
    }
}

@Composable
private fun ItemRow(label: String, info: RestrictionManager.TravelInfo?) {
    val statusColor = when (info?.level) {
        RestrictionManager.Level.DANGER -> Color(0xFFFF453A)
        RestrictionManager.Level.CAUTION -> Color(0xFFFF9F0A)
        RestrictionManager.Level.INFO -> Color(0xFF30D158)
        else -> Color.Gray
    }

    val statusText = when (info?.level) {
        RestrictionManager.Level.DANGER -> "NOT ALLOWED"
        RestrictionManager.Level.CAUTION -> "CAREFUL"
        RestrictionManager.Level.INFO -> "ALLOWED"
        else -> "UNKNOWN"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label.uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D1B5E)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = info?.message ?: "No details available.",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
            
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

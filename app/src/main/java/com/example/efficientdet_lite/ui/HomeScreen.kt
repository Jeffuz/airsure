package com.example.efficientdet_lite.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onScanCarryOnClick: () -> Unit,
    onAnnouncementsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "AirSure",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "On-device travel assistant for carry-on checks and airport announcements.",
            style = MaterialTheme.typography.bodyLarge
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Scan Carry-On",
                    style = MaterialTheme.typography.titleLarge
                )

                Text("Use on-device vision to detect common packing risks before airport security.")

                Button(
                    onClick = onScanCarryOnClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Scanner")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Announcement Listener",
                    style = MaterialTheme.typography.titleLarge
                )

                Text("Extract gate changes, boarding calls, and delays from airport announcements.")

                Button(
                    onClick = onAnnouncementsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Listener")
                }
            }
        }
    }
}

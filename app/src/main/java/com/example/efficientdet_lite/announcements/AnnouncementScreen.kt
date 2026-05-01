package com.example.efficientdet_lite.announcements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AnnouncementScreen(
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = onBackClick) {
            Text("Back")
        }

        Text("Announcement Listener Placeholder")
    }
}

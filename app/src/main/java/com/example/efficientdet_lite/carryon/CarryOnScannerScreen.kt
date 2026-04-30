package com.example.efficientdet_lite.carryon

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.efficientdet_lite.vision.EfficientDetCameraScreen

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

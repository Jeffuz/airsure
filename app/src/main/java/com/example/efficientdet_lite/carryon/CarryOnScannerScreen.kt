package com.example.efficientdet_lite.carryon

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.efficientdet_lite.vision.EfficientDetCameraScreen
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun CarryOnScannerScreen(
    onBackClick: () -> Unit
) {
    var selectedCountry by remember { mutableStateOf("United States") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = onBackClick) {
            Text("Back")
        }

        EfficientDetCameraScreen(selectedCountry = selectedCountry)
    }
}

package com.example.efficientdet_lite.carryon

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.efficientdet_lite.vision.EfficientDetCameraScreen
import com.qualcomm.qti.objectdetection.RestrictionManager

@Composable
fun CarryOnScannerScreen(
    onBackClick: () -> Unit,
    onSubmitClick: (List<Pair<String, RestrictionManager.TravelInfo?>>) -> Unit
) {
    var selectedCountry by remember { mutableStateOf("United States") }

    Box(modifier = Modifier.fillMaxSize()) {
        EfficientDetCameraScreen(
            selectedCountry = selectedCountry,
            onSubmit = onSubmitClick
        )
    }
}

package com.example.efficientdet_lite.vision

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.efficientdet_lite.ui.BottomNavBar
import com.qualcomm.qti.objectdetection.ObjectDetection
import com.qualcomm.qti.objectdetection.ObjectDetectionAnalyzer
import com.qualcomm.qti.objectdetection.RestrictionManager
import com.qualcomm.tflite.AIHubDefaults
import java.util.concurrent.Executors

@Composable
fun EfficientDetCameraScreen(
    selectedCountry: String = "United States",
    onItemDetected: (List<Pair<String, RestrictionManager.TravelInfo?>>) -> Unit = {},
    onSubmit: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onListenClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val inferenceExecutor = remember { Executors.newSingleThreadExecutor() }

    val detector = remember {
        ObjectDetection(
            context,
            "detector.tflite",
            "labels.txt",
            AIHubDefaults.acceleratorPriorityOrder
        )
    }

    val tracker = remember { DetectionTracker() }

    var result by remember { mutableStateOf(EfficientDetFrameResult()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val analyzer = remember(detector, selectedCountry) {
        ObjectDetectionAnalyzer(
            context = context,
            detector = detector,
            inferenceExecutor = inferenceExecutor,
            onResult = { frameResult ->
                mainExecutor.execute {
                    val trackedDetections = tracker.update(frameResult.detections)
                    result = frameResult.copy(detections = trackedDetections)
                    errorMessage = null

                    if (trackedDetections.isNotEmpty()) {
                        onItemDetected(
                            trackedDetections.map {
                                it.label to it.travelInfo
                            }
                        )
                    }
                }
            },
            onError = { throwable ->
                Log.e(TAG, "Camera inference error", throwable)
                mainExecutor.execute {
                    errorMessage = throwable.message ?: throwable::class.java.simpleName
                }
            },
            selectedCountry = selectedCountry
        )
    }

    var lensFacing by remember {
        mutableIntStateOf(CameraSelector.LENS_FACING_BACK)
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            ProcessCameraProvider.getInstance(context).get().unbindAll()
            detector.close()
            cameraExecutor.shutdown()
            inferenceExecutor.shutdown()
        }
    }

    DisposableEffect(hasCameraPermission, lensFacing, analyzer) {
        if (!hasCameraPermission) {
            onDispose { }
        } else {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor, analyzer)
                        }

                    runCatching {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )

                        Log.i(
                            TAG,
                            "Bound ${
                                if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                                    "front"
                                } else {
                                    "rear"
                                }
                            } camera"
                        )
                    }.onFailure { throwable ->
                        Log.e(TAG, "Camera bind failed", throwable)
                        errorMessage = throwable.message ?: throwable::class.java.simpleName
                    }
                },
                mainExecutor
            )

            onDispose {
                ProcessCameraProvider.getInstance(context).get().unbindAll()
                tracker.reset()
                result = EfficientDetFrameResult()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            DetectionOverlay(
                result = result,
                isFrontCamera = lensFacing == CameraSelector.LENS_FACING_FRONT,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = "Camera permission is required",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        ScannerTopBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            onBackClick = onBackClick
        )

        errorMessage?.let { message ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 82.dp)
                    .padding(horizontal = 22.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.72f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            ScannerBottomActions(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .padding(bottom = 18.dp),
                onFinishScanClick = onSubmit,
                onSwitchCameraClick = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                }
            )

            BottomNavBar(
                activeRoute = "scan",
                onHomeClick = onHomeClick,
                onScanClick = { },
                onListenClick = onListenClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ScannerTopBar(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.97f))
                .border(
                    width = 1.dp,
                    color = Color(0xFFE0E7F5),
                    shape = RoundedCornerShape(999.dp)
                )
                .clickable { onBackClick() }
                .padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF0D1B5E),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Back",
                color = Color(0xFF0D1B5E),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.95f))
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
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF2A7CFF),
                                Color(0xFF006BFF)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "••",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Analyzing locally",
                color = Color(0xFF006BFF),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

// Finish scan and switch camera buttons
@Composable
private fun ScannerBottomActions(
    modifier: Modifier = Modifier,
    onFinishScanClick: () -> Unit,
    onSwitchCameraClick: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onFinishScanClick,
            modifier = Modifier
                .weight(0.95f)
                .height(58.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF006BFF),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(999.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Finish scan",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }

        Button(
            onClick = onSwitchCameraClick,
            modifier = Modifier
                .weight(1.05f)
                .height(58.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black.copy(alpha = 0.46f),
                contentColor = Color.White
            ),
            border = BorderStroke(
                width = 1.5.dp,
                color = Color.White.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(999.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.FlipCameraAndroid,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Flip camera",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

private const val TAG = "EfficientDetCameraScreen"
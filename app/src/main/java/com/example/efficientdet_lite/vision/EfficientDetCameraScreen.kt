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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.qualcomm.qti.objectdetection.RestrictionManager
import com.qualcomm.qti.objectdetection.ObjectDetection
import com.qualcomm.qti.objectdetection.ObjectDetectionAnalyzer
import com.qualcomm.tflite.AIHubDefaults
import java.util.concurrent.Executors

@Composable
fun EfficientDetCameraScreen(
    selectedCountry: String = "United States",
    onSubmit: (List<Pair<String, RestrictionManager.TravelInfo?>>) -> Unit = {}
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
                    result = frameResult.copy(detections = tracker.update(frameResult.detections))
                    errorMessage = null
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

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
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
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .build()
                        .also { it.setAnalyzer(cameraExecutor, analyzer) }

                    runCatching {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
                        Log.i(TAG, "Bound ${if (lensFacing == CameraSelector.LENS_FACING_FRONT) "front" else "rear"} camera")
                    }.onFailure { throwable ->
                        Log.e(TAG, "Camera bind failed", throwable)
                        errorMessage = throwable.message ?: throwable::class.java.simpleName
                    }
                },
                mainExecutor,
            )
            onDispose {
                ProcessCameraProvider.getInstance(context).get().unbindAll()
                tracker.reset()
                result = EfficientDetFrameResult()
            }
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize(),
            )
            DetectionOverlay(
                result = result,
                isFrontCamera = lensFacing == CameraSelector.LENS_FACING_FRONT,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = "Camera permission is required",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        Surface(
            color = Color.Black.copy(alpha = 0.55f),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 12.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Destination: $selectedCountry",
                    color = Color(0xFFADD8E6),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = {
                    val currentItems = result.detections.map { it.label to it.travelInfo }
                    onSubmit(currentItems)
                },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Submit", style = MaterialTheme.typography.titleMedium)
            }

            errorMessage?.let {
                Surface(color = Color.Black.copy(alpha = 0.65f), shape = MaterialTheme.shapes.small) {
                    Text(
                        text = it,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

private const val TAG = "EfficientDetCameraScreen"

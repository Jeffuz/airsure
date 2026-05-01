package com.example.efficientdet_lite.vision

import android.graphics.RectF
import com.qualcomm.qti.objectdetection.RestrictionManager

data class Detection(
    val box: RectF,
    val label: String,
    val confidence: Float,
    val travelInfo: RestrictionManager.TravelInfo? = null,
    val classIdx: Int = 0
)

data class FrameMetadata(
    val sourceWidth: Int,
    val sourceHeight: Int,
    val inputSize: Int,
    val scale: Float,
    val padX: Float,
    val padY: Float,
)

data class PreprocessedFrame(
    val input: ByteArray,
    val metadata: FrameMetadata,
)

data class EfficientDetFrameResult(
    val detections: List<Detection> = emptyList(),
    val frameWidth: Int = 0,
    val frameHeight: Int = 0,
    val backend: String = "",
)

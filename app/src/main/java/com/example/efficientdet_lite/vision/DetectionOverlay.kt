package com.example.efficientdet_lite.vision

import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import com.qualcomm.qti.objectdetection.RestrictionManager
import kotlin.math.max

@Composable
fun DetectionOverlay(
    result: EfficientDetFrameResult,
    isFrontCamera: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (result.frameWidth <= 0 || result.frameHeight <= 0) return@Canvas

        val scale = max(size.width / result.frameWidth, size.height / result.frameHeight)
        val dx = (size.width - result.frameWidth * scale) / 2f
        val dy = (size.height - result.frameHeight * scale) / 2f

        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(82, 222, 151)
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 34f
            typeface = Typeface.DEFAULT_BOLD
        }
        val labelBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(220, 24, 30, 36)
            style = Paint.Style.FILL
        }
        val restrictionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 28f
            typeface = Typeface.DEFAULT
        }

        drawIntoCanvas { canvas ->
            val nativeCanvas = canvas.nativeCanvas
            result.detections.forEach { detection ->
                val sourceBox = if (isFrontCamera) {
                    RectF(
                        result.frameWidth - detection.box.right,
                        detection.box.top,
                        result.frameWidth - detection.box.left,
                        detection.box.bottom,
                    )
                } else {
                    detection.box
                }
                val box = RectF(
                    sourceBox.left * scale + dx,
                    sourceBox.top * scale + dy,
                    sourceBox.right * scale + dx,
                    sourceBox.bottom * scale + dy,
                )
                
                // Use level-based colors for box
                val boxColor = when (detection.travelInfo?.level) {
                    RestrictionManager.Level.DANGER -> android.graphics.Color.RED
                    RestrictionManager.Level.CAUTION -> android.graphics.Color.rgb(255, 152, 0)
                    else -> android.graphics.Color.rgb(82, 222, 151)
                }
                boxPaint.color = boxColor
                nativeCanvas.drawRect(box, boxPaint)

                val label = "${detection.label.uppercase()} ${(detection.confidence * 100).toInt()}%"
                val textWidth = labelPaint.measureText(label)
                val textHeight = labelPaint.textSize
                
                labelBackgroundPaint.color = boxColor
                val labelRect = RectF(
                    box.left,
                    max(0f, box.top - textHeight - 12f),
                    box.left + textWidth + 18f,
                    max(textHeight + 12f, box.top),
                )
                nativeCanvas.drawRoundRect(labelRect, 6f, 6f, labelBackgroundPaint)
                nativeCanvas.drawText(label, labelRect.left + 9f, labelRect.bottom - 10f, labelPaint)
                
                // Draw travel info
                detection.travelInfo?.let { info ->
                    val bgColor = when (info.level) {
                        RestrictionManager.Level.DANGER -> android.graphics.Color.argb(200, 244, 67, 54)
                        RestrictionManager.Level.CAUTION -> android.graphics.Color.argb(200, 255, 152, 0)
                        else -> android.graphics.Color.argb(200, 76, 175, 80)
                    }
                    labelBackgroundPaint.color = bgColor
                    
                    val words = info.message.split(" ")
                    val maxWidth = max(300f, size.width * 0.45f)
                    val lines = mutableListOf<String>()
                    var currentLine = StringBuilder()
                    
                    for (word in words) {
                        if (restrictionPaint.measureText(currentLine.toString() + word) < maxWidth) {
                            currentLine.append(word).append(" ")
                        } else {
                            lines.add(currentLine.toString())
                            currentLine = StringBuilder(word).append(" ")
                        }
                    }
                    lines.add(currentLine.toString())
                    
                    val maxLineWidth = lines.maxOf { restrictionPaint.measureText(it) }
                    val lineHeight = 38f
                    val infoRect = RectF(
                        box.left,
                        box.bottom,
                        box.left + maxLineWidth + 16f,
                        box.bottom + (lines.size * lineHeight) + 10f
                    )
                    
                    nativeCanvas.drawRect(infoRect, labelBackgroundPaint)
                    lines.forEachIndexed { index, line ->
                        nativeCanvas.drawText(
                            line,
                            infoRect.left + 8f,
                            box.bottom + 35f + (index * lineHeight),
                            restrictionPaint
                        )
                    }
                }
            }
        }

        drawRect(Color.Transparent, size = Size(size.width, size.height))
    }
}

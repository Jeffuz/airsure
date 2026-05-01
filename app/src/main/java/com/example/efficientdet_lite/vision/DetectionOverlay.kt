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
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val labelBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
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
                
                // 1. Determine Colors and Status Text
                val info = detection.travelInfo
                val (primaryColor, statusHeader) = when (info?.level) {
                    RestrictionManager.Level.DANGER -> android.graphics.Color.RED to "NOT ALLOWED"
                    RestrictionManager.Level.CAUTION -> android.graphics.Color.rgb(255, 159, 10) to "CAREFUL"
                    RestrictionManager.Level.INFO -> android.graphics.Color.rgb(48, 209, 88) to "ALLOWED"
                    else -> android.graphics.Color.rgb(100, 210, 255) to "SCANNING"
                }

                // 2. Draw the Main Bounding Box
                boxPaint.color = primaryColor
                nativeCanvas.drawRect(box, boxPaint)

                // 3. Draw the Top Label (Item Name + Status Header)
                val topLabel = "${detection.label.uppercase()} - $statusHeader"
                val topLabelWidth = labelPaint.measureText(topLabel)
                val topLabelHeight = labelPaint.textSize
                
                labelBackgroundPaint.color = primaryColor
                val topLabelRect = RectF(
                    box.left,
                    max(0f, box.top - topLabelHeight - 16f),
                    box.left + topLabelWidth + 24f,
                    box.top
                )
                nativeCanvas.drawRect(topLabelRect, labelBackgroundPaint)
                nativeCanvas.drawText(topLabel, topLabelRect.left + 12f, topLabelRect.bottom - 12f, labelPaint)
                
                // 4. Draw the Bottom Info Card (Detailed Message)
                detection.travelInfo?.let { info ->
                    val infoBgColor = android.graphics.Color.argb(230, 28, 28, 30) // Dark semi-transparent background
                    labelBackgroundPaint.color = infoBgColor
                    
                    val words = info.message.split(" ")
                    val maxWidth = max(350f, box.width() + 40f)
                    val lines = mutableListOf<String>()
                    var currentLine = StringBuilder()
                    
                    for (word in words) {
                        if (restrictionPaint.measureText(currentLine.toString() + word) < maxWidth - 30f) {
                            currentLine.append(word).append(" ")
                        } else {
                            lines.add(currentLine.toString().trim())
                            currentLine = StringBuilder(word).append(" ")
                        }
                    }
                    if (currentLine.isNotEmpty()) lines.add(currentLine.toString().trim())
                    
                    val maxLineWidth = if (lines.isNotEmpty()) lines.maxOf { restrictionPaint.measureText(it) } else 0f
                    val contentWidth = max(maxLineWidth + 32f, topLabelWidth + 24f)
                    val lineHeight = 38f
                    val infoHeight = (lines.size * lineHeight) + 24f
                    
                    val infoRect = RectF(
                        box.left,
                        box.bottom,
                        box.left + contentWidth,
                        box.bottom + infoHeight
                    )
                    
                    // Draw Card Background
                    nativeCanvas.drawRect(infoRect, labelBackgroundPaint)
                    
                    // Draw Left Accent Border
                    boxPaint.strokeWidth = 10f
                    nativeCanvas.drawLine(infoRect.left, infoRect.top, infoRect.left, infoRect.bottom, boxPaint)
                    boxPaint.strokeWidth = 6f // Reset
                    
                    // Draw Text Lines
                    lines.forEachIndexed { index, line ->
                        nativeCanvas.drawText(
                            line,
                            infoRect.left + 20f,
                            box.bottom + 42f + (index * lineHeight),
                            restrictionPaint
                        )
                    }
                }
            }
        }

        drawRect(Color.Transparent, size = Size(size.width, size.height))
    }
}

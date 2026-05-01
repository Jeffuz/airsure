package com.qualcomm.qti.objectdetection

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.efficientdet_lite.vision.Detection
import com.example.efficientdet_lite.vision.EfficientDetFrameResult
import com.example.efficientdet_lite.vision.EfficientDetPreprocessor
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean
import android.graphics.RectF
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.content.Context
import java.util.Locale

class ObjectDetectionAnalyzer(
    private val context: Context,
    private val detector: ObjectDetection,
    private val inferenceExecutor: ExecutorService,
    private val onResult: (EfficientDetFrameResult) -> Unit,
    private val onError: (Throwable) -> Unit,
    private val selectedCountry: String = "United States"
) : ImageAnalysis.Analyzer {
    private val inferenceBusy = AtomicBoolean(false)
    private var tts: TextToSpeech? = null
    private val announcedLabels = mutableSetOf<String>()
    private var lastAnnouncementTime = 0L
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    init {
        tts = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                tts?.language = Locale.US
            }
        }
    }

    override fun analyze(image: ImageProxy) {
        if (!inferenceBusy.compareAndSet(false, true)) {
            image.close()
            return
        }

        val bitmap = try {
            EfficientDetPreprocessor.imageProxyToBitmap(image)
        } catch (throwable: Throwable) {
            image.close()
            inferenceBusy.set(false)
            onError(throwable)
            return
        }
        image.close()

        inferenceExecutor.execute {
            try {
                val bbList = ArrayList<RectangleBox>()
                detector.predict(bitmap, 90, bbList)

                var dangerDetected = false
                val detections = bbList.map { box ->
                    box.travelInfo = RestrictionManager.getRestriction(box.label, selectedCountry)
                    if (box.travelInfo.level == RestrictionManager.Level.DANGER) {
                        dangerDetected = true
                    }
                    
                    // TTS Announcement logic
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastAnnouncementTime > 3000) {
                        if (!announcedLabels.contains(box.label)) {
                            announceItem(box.label, box.travelInfo.message)
                            announcedLabels.add(box.label)
                            lastAnnouncementTime = currentTime
                            if (announcedLabels.size > 5) announcedLabels.clear()
                        }
                    }

                    Detection(
                        box = RectF(box.left, box.top, box.right, box.bottom),
                        label = box.label,
                        confidence = box.confidence,
                        travelInfo = box.travelInfo,
                        classIdx = box.classIdx
                    )
                }

                if (dangerDetected) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                }

                onResult(
                    EfficientDetFrameResult(
                        detections = detections,
                        frameWidth = bitmap.width,
                        frameHeight = bitmap.height,
                        backend = "LiteRT (Old Logic)"
                    )
                )
            } catch (throwable: Throwable) {
                Log.e(TAG, "ObjectDetection inference failed", throwable)
                onError(throwable)
            } finally {
                if (!bitmap.isRecycled) bitmap.recycle()
                inferenceBusy.set(false)
            }
        }
    }

    private fun announceItem(label: String, message: String) {
        val text = "$label. $message"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun close() {
        tts?.stop()
        tts?.shutdown()
    }

    private companion object {
        const val TAG = "ObjectDetectionAnalyzer"
    }
}

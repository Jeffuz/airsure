package com.example.efficientdet_lite.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

class AudioRecorder {
    companion object {
        const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
    }

    @SuppressLint("MissingPermission")
    fun startRecording(): Flow<FloatArray> = flow {
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            BUFFER_SIZE
        )

        val buffer = ShortArray(BUFFER_SIZE)
        audioRecord.startRecording()

        try {
            while (coroutineContext.isActive) {
                val read = audioRecord.read(buffer, 0, buffer.size)
                if (read > 0) {
                    // Convert PCM16 to FloatArray [-1.0, 1.0]
                    val floatBuffer = FloatArray(read) { buffer[it] / 32768.0f }
                    emit(floatBuffer)
                }
            }
        } finally {
            audioRecord.stop()
            audioRecord.release()
        }
    }
}

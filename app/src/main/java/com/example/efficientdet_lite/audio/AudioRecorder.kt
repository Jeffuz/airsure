package com.example.efficientdet_lite.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

import android.util.Log

class AudioRecorder {
    companion object {
        private const val TAG = "AudioRecorder"
        const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    @SuppressLint("MissingPermission")
    fun startRecording(): Flow<FloatArray> = flow {
        val minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        
        if (minBufferSize <= 0) {
            Log.e(TAG, "Invalid minBufferSize: $minBufferSize")
            return@flow
        }

        // Whisper often works better with larger buffers or specific sizes
        val bufferSize = (minBufferSize * 2).coerceAtLeast(3200) 
        
        val audioRecord = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create AudioRecord", e)
            return@flow
        }

        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord not initialized")
            audioRecord.release()
            return@flow
        }

        val buffer = ShortArray(bufferSize)
        try {
            audioRecord.startRecording()
            Log.d(TAG, "Started recording")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            audioRecord.release()
            return@flow
        }

        try {
            while (coroutineContext.isActive) {
                val read = audioRecord.read(buffer, 0, buffer.size)
                if (read > 0) {
                    val floatBuffer = FloatArray(read) { buffer[it] / 32768.0f }
                    emit(floatBuffer)
                } else if (read < 0) {
                    Log.e(TAG, "Error reading audio: $read")
                    break
                }
            }
        } finally {
            Log.d(TAG, "Stopping recording")
            runCatching { audioRecord.stop() }
            audioRecord.release()
        }
    }
}

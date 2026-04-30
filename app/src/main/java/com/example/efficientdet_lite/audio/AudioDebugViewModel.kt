package com.example.efficientdet_lite.audio

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class AudioDebugViewModel(application: Application) : AndroidViewModel(application) {
    private val audioRecorder = AudioRecorder()
    private val whisperModel = WhisperModel(application)
    private var recordingJob: Job? = null
    
    var amplitude by mutableStateOf(0f)
        private set
    
    var isRecording by mutableStateOf(false)
        private set

    var transcription by mutableStateOf("Press start and speak...")
        private set

    private val audioBuffer = mutableListOf<Float>()

    fun toggleRecording() {
        if (isRecording) {
            stop()
        } else {
            start()
        }
    }

    private fun start() {
        isRecording = true
        transcription = "Listening (wait 5s)..."
        audioBuffer.clear()
        
        recordingJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                audioRecorder.startRecording().collect { buffer ->
                    val peak = buffer.maxOfOrNull { abs(it) } ?: 0f
                    
                    audioBuffer.addAll(buffer.toList())
                    
                    // Wait for 5 seconds of audio for better recognition
                    if (audioBuffer.size >= 16000 * 5) {
                        val currentAudio = audioBuffer.toFloatArray()
                        audioBuffer.clear() 
                        
                        withContext(Dispatchers.Main) { transcription = "Transcribing..." }
                        
                        val result = whisperModel.transcribe(currentAudio)
                        
                        withContext(Dispatchers.Main) {
                            transcription = result.ifBlank { "(No speech detected)" }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        amplitude = peak
                    }
                }
            } catch (e: Exception) {
                Log.e("AudioDebug", "Recording failed", e)
                withContext(Dispatchers.Main) {
                    isRecording = false
                    transcription = "Error: ${e.message}"
                }
            }
        }
    }

    private fun stop() {
        isRecording = false
        recordingJob?.cancel()
        amplitude = 0f
        transcription = "Stopped."
    }

    fun runAssetTest() {
        viewModelScope.launch(Dispatchers.Default) {
            withContext(Dispatchers.Main) { transcription = "Running asset test..." }
            val result = whisperModel.transcribeFromAsset("test_audio.wav")
            withContext(Dispatchers.Main) {
                transcription = "Test Result: $result"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        whisperModel.close()
    }
}

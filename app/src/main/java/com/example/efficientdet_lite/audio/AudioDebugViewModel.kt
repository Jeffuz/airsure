package com.example.efficientdet_lite.audio

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

class AudioDebugViewModel : ViewModel() {
    private val audioRecorder = AudioRecorder()
    private var recordingJob: Job? = null
    
    var amplitude by mutableStateOf(0f)
        private set
    
    var isRecording by mutableStateOf(false)
        private set

    fun toggleRecording() {
        if (isRecording) {
            stop()
        } else {
            start()
        }
    }

    private fun start() {
        isRecording = true
        recordingJob = viewModelScope.launch {
            audioRecorder.startRecording().collect { buffer ->
                // Calculate peak amplitude for visualization
                amplitude = buffer.maxOfOrNull { abs(it) } ?: 0f
            }
        }
    }

    private fun stop() {
        isRecording = false
        recordingJob?.cancel()
        amplitude = 0f
    }
}

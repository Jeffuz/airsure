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

class AudioViewModel(application: Application) : AndroidViewModel(application) {
    private val audioRecorder = AudioRecorder()
    private val whisperModel = WhisperModel(application)
    private val parser = AnnouncementParser()
    
    private var recordingJob: Job? = null
    
    var transcription by mutableStateOf("")
        private set
    
    var lastAnnouncement by mutableStateOf<Announcement?>(null)
        private set

    var isRecording by mutableStateOf(false)
        private set

    fun startListening() {
        if (isRecording) return
        isRecording = true
        transcription = "Listening..."
        
        recordingJob = viewModelScope.launch(Dispatchers.Default) {
            audioRecorder.startRecording().collect { buffer ->
                // In a real app, we would accumulate buffer until we have ~1-3s of audio
                // For this step, we'll just try to transcribe the current chunk
                val result = whisperModel.transcribe(buffer)
                
                launch(Dispatchers.Main) {
                    if (result.isNotBlank() && result != "Transcription placeholder") {
                        transcription = result
                        val announcement = parser.parse(result)
                        lastAnnouncement = announcement
                    }
                }
            }
        }
    }

    fun stopListening() {
        isRecording = false
        recordingJob?.cancel()
        transcription = ""
    }

    override fun onCleared() {
        super.onCleared()
        whisperModel.close()
    }
}

package com.example.efficientdet_lite.audio

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.efficientdet_lite.announcements.Announcement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private val pendingAudio = mutableListOf<Float>()
    private val targetSamples = 16000 * 5 // 5 seconds of audio at 16kHz

    fun startListening() {
        if (isRecording) return
        isRecording = true
        transcription = "Listening..."
        pendingAudio.clear()
        
        recordingJob = viewModelScope.launch(Dispatchers.Default) {
            audioRecorder.startRecording().collect { chunk ->
                pendingAudio.addAll(chunk.toList())
                
                if (pendingAudio.size >= targetSamples) {
                    val audioForModel = pendingAudio.take(targetSamples).toFloatArray()
                    // Clear some audio, but keep a small overlap (e.g. 1s) for continuity
                    val overlap = 16000 * 1
                    val remaining = if (pendingAudio.size > targetSamples) {
                        pendingAudio.subList(targetSamples - overlap, pendingAudio.size).toList()
                    } else emptyList()
                    
                    pendingAudio.clear()
                    pendingAudio.addAll(remaining)

                    val result = whisperModel.transcribe(audioForModel)
                    
                    withContext(Dispatchers.Main) {
                        if (result.isNotBlank() && result != "No speech detected") {
                            transcription = result
                            val announcement = parser.parse(result)
                            if (announcement != null) {
                                lastAnnouncement = announcement
                            }
                        }
                    }
                }
            }
        }
    }

    fun stopListening() {
        isRecording = false
        recordingJob?.cancel()
        transcription = ""
        pendingAudio.clear()
    }

    override fun onCleared() {
        super.onCleared()
        whisperModel.close()
    }
}

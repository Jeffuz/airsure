package com.example.efficientdet_lite.audio

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.efficientdet_lite.announcements.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class AudioDebugViewModel(
    application: Application,
    private val flightViewModel: FlightViewModel
) : AndroidViewModel(application) {
    private val audioRecorder = AudioRecorder()
    private val whisperModel = WhisperModel(application)
    private var recordingJob: Job? = null

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val CHUNK_SECONDS = 5
        private const val OVERLAP_SECONDS = 1
        private const val CHUNK_SIZE = SAMPLE_RATE * CHUNK_SECONDS
        private const val OVERLAP_SIZE = SAMPLE_RATE * OVERLAP_SECONDS
        
        // Silence detection constants
        private const val SILENCE_THRESHOLD = 0.05f
        private const val SILENCE_DURATION_MS = 1500L
        private const val MAX_RECORDING_MS = 15000L // 15s max to prevent OOM
    }

    // --- State Variables ---
    var amplitude by mutableStateOf(0f)
        private set
    
    var isRecording by mutableStateOf(false)
        private set

    var transcription by mutableStateOf("AI not loaded. Tap below.")
        private set

    var isAILoaded by mutableStateOf(false)
        private set

    val activeAlert get() = flightViewModel.activeAlert

    // Helper to get flight info from global state
    private val userFlight: UserFlight get() = flightViewModel.userFlight ?: UserFlight("AA123", "LAX", "JFK")

    private val audioBuffer = mutableListOf<Float>()
    private var lastSpeechTime = 0L
    private var hasSpeechStarted = false

    // --- Core Logic ---

    fun loadAI() {
        viewModelScope.launch {
            try {
                whisperModel.loadModels { progress ->
                    transcription = progress
                }
                isAILoaded = whisperModel.isLoaded
            } catch (e: Exception) {
                Log.e("AudioDebug", "Failed to load models", e)
                transcription = "Error: ${e.message}"
            } catch (e: Error) {
                Log.e("AudioDebug", "Critical error", e)
                transcription = "OOM / Critical Error"
            }
        }
    }

    /**
     * Unified handler for all transcriptions (Mic or Asset)
     */
    private suspend fun handleTranscript(text: String) {
        withContext(Dispatchers.Main) {
            transcription = text.ifBlank { "(No speech detected)" }
            
            // Run matching logic
            val announcement = AnnouncementProcessor.process(text, userFlight)
            if (announcement != null) {
                Log.i("AudioDebug", "MATCH FOUND: ${announcement.type} for ${announcement.matchedFlightNumber}")
                // Update global state
                flightViewModel.setAlert(announcement)
            }
        }
    }

    fun toggleRecording() {
        if (isRecording) {
            stop()
        } else {
            start()
        }
    }

    private fun start() {
        isRecording = true
        flightViewModel.clearAlert() // Reset alert when starting new listen
        transcription = "Listening..."
        lastSpeechTime = System.currentTimeMillis()
        hasSpeechStarted = false
        synchronized(audioBuffer) { audioBuffer.clear() }
        
        recordingJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                audioRecorder.startRecording().collect { buffer ->
                    val peak = buffer.maxOfOrNull { abs(it) } ?: 0f
                    val currentTime = System.currentTimeMillis()
                    
                    var toTranscribe: FloatArray? = null
                    
                    synchronized(audioBuffer) {
                        audioBuffer.addAll(buffer.toList())
                        
                        // Speech detection logic
                        if (peak > SILENCE_THRESHOLD) {
                            lastSpeechTime = currentTime
                            hasSpeechStarted = true
                        }

                        val silenceDuration = currentTime - lastSpeechTime
                        val totalDuration = (audioBuffer.size.toFloat() / SAMPLE_RATE) * 1000

                        // Trigger transcription if:
                        // 1. We had some speech and now it's silent for long enough
                        // 2. OR we reached max recording length
                        if ((hasSpeechStarted && silenceDuration >= SILENCE_DURATION_MS) || totalDuration >= MAX_RECORDING_MS) {
                            if (audioBuffer.isNotEmpty()) {
                                toTranscribe = audioBuffer.toFloatArray()
                                audioBuffer.clear()
                                hasSpeechStarted = false
                                lastSpeechTime = currentTime
                            }
                        }
                    }

                    toTranscribe?.let { audioData ->
                        withContext(Dispatchers.Main) { transcription = "Transcribing..." }
                        val result = whisperModel.transcribe(audioData)
                        handleTranscript(result)
                        withContext(Dispatchers.Main) { 
                            if (isRecording) transcription = "Listening again..." 
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

    fun runAssetTest(fileName: String = "test_audio.wav") {
        viewModelScope.launch(Dispatchers.Default) {
            flightViewModel.clearAlert()
            withContext(Dispatchers.Main) { transcription = "Running $fileName..." }
            
            // Look in the 'audio/' subfolder
            val path = if (fileName == "test_audio.wav") fileName else "audio/$fileName"
            val result = whisperModel.transcribeFromAsset(path)
            handleTranscript(result)
        }
    }

    fun runLogicTest() {
        viewModelScope.launch {
            flightViewModel.clearAlert()
            val testTranscript = "Attention passengers, American Airlines flight 123 is now boarding at Gate 24B."
            transcription = testTranscript
            
            // This will trigger handleTranscript just like a real voice would
            handleTranscript(testTranscript)
        }
    }

    fun clearAlert() {
        flightViewModel.clearAlert()
    }

    override fun onCleared() {
        super.onCleared()
        whisperModel.close()
    }

    /**
     * Factory to inject FlightViewModel
     */
    class Factory(
        private val application: Application,
        private val flightViewModel: FlightViewModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AudioDebugViewModel(application, flightViewModel) as T
        }
    }
}

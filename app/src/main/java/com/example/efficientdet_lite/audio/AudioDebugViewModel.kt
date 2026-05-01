package com.example.efficientdet_lite.audio

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.efficientdet_lite.announcements.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class AudioDebugViewModel(application: Application) : AndroidViewModel(application) {
    private val audioRecorder = AudioRecorder()
    private val whisperModel = WhisperModel(application)
    private var recordingJob: Job? = null

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val CHUNK_SECONDS = 5
        private const val OVERLAP_SECONDS = 1
        private const val CHUNK_SIZE = SAMPLE_RATE * CHUNK_SECONDS
        private const val OVERLAP_SIZE = SAMPLE_RATE * OVERLAP_SECONDS
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

    // For the MVP Demo: Track a specific flight
    var userFlight by mutableStateOf(UserFlight(
        flightNumber = "AA123",
        from = "LAX",
        to = "JFK",
        gate = null
    ))
        private set

    // The result of the parsing logic
    var activeAlert by mutableStateOf<FlightAnnouncement?>(null)
        private set

    private val audioBuffer = mutableListOf<Float>()

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
                activeAlert = announcement
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
        activeAlert = null // Reset alert when starting new listen
        transcription = "Listening..."
        synchronized(audioBuffer) { audioBuffer.clear() }
        
        recordingJob = viewModelScope.launch(Dispatchers.Default) {
            try {
                audioRecorder.startRecording().collect { buffer ->
                    val peak = buffer.maxOfOrNull { abs(it) } ?: 0f
                    
                    var toTranscribe: FloatArray? = null
                    
                    synchronized(audioBuffer) {
                        audioBuffer.addAll(buffer.toList())
                        if (audioBuffer.size >= CHUNK_SIZE) {
                            toTranscribe = audioBuffer.toFloatArray()
                            val overlap = audioBuffer.takeLast(OVERLAP_SIZE)
                            audioBuffer.clear()
                            audioBuffer.addAll(overlap)
                        }
                    }

                    toTranscribe?.let { audioData ->
                        withContext(Dispatchers.Main) { transcription = "Transcribing..." }
                        val result = whisperModel.transcribe(audioData)
                        handleTranscript(result)
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
            activeAlert = null
            withContext(Dispatchers.Main) { transcription = "Running $fileName..." }
            
            // Look in the 'audio/' subfolder
            val path = if (fileName == "test_audio.wav") fileName else "audio/$fileName"
            val result = whisperModel.transcribeFromAsset(path)
            handleTranscript(result)
        }
    }

    fun runLogicTest() {
        viewModelScope.launch {
            activeAlert = null
            val testTranscript = "Attention passengers, American Airlines flight 123 is now boarding at Gate 24B."
            transcription = testTranscript
            
            // This will trigger handleTranscript just like a real voice would
            handleTranscript(testTranscript)
            
            Log.d("MVP_TEST", "User Flight: ${userFlight.flightNumber}")
            Log.d("MVP_TEST", "Processed Result: $activeAlert")
        }
    }

    fun clearAlert() {
        activeAlert = null
    }

    override fun onCleared() {
        super.onCleared()
        whisperModel.close()
    }
}

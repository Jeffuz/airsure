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
import com.example.efficientdet_lite.announcements.Announcement
import com.example.efficientdet_lite.announcements.AnnouncementProcessor
import com.example.efficientdet_lite.announcements.FlightAnnouncement
import com.example.efficientdet_lite.app.TripDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class AudioDebugViewModel(
    application: Application,
    private val tripDetails: TripDetails
) : AndroidViewModel(application) {

    private val audioRecorder = AudioRecorder()
    private val whisperModel = WhisperModel(application)
    private var recordingJob: Job? = null

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val SILENCE_THRESHOLD = 0.05f
        private const val SILENCE_DURATION_MS = 1500L
        private const val MAX_RECORDING_MS = 15000L
    }

    var amplitude by mutableStateOf(0f)
        private set

    var isRecording by mutableStateOf(false)
        private set

    var transcription by mutableStateOf("Loading Whisper AI...")
        private set

    var isAILoaded by mutableStateOf(false)
        private set

    var isLoadingAI by mutableStateOf(false)
        private set

    var loadingMessage by mutableStateOf("Preparing local announcement AI...")
        private set

    var loadingProgress by mutableStateOf(0f)
        private set

    var loadError by mutableStateOf<String?>(null)
        private set

    var activeAlert by mutableStateOf<FlightAnnouncement?>(null)
        private set

    private val currentTrip: TripDetails
        get() = tripDetails

    private val audioBuffer = mutableListOf<Float>()
    private var lastSpeechTime = 0L
    private var hasSpeechStarted = false

    // --- Core Logic ---

    fun loadAI() {
        if (isAILoaded || isLoadingAI) return

        viewModelScope.launch {
            isLoadingAI = true
            loadError = null
            loadingMessage = "Preparing local announcement AI..."
            loadingProgress = 0.05f

            try {
                whisperModel.loadModels { progress ->
                    updateLoadingState(progress)
                }

                isAILoaded = whisperModel.isLoaded

                if (isAILoaded) {
                    transcription = "Ready to listen."
                    loadingMessage = "AI ready"
                    loadingProgress = 1f
                }
            } catch (e: Exception) {
                Log.e("AudioDebug", "Failed to load models", e)
                loadError = e.message ?: "Unknown error"
                transcription = "Error: ${e.message}"
                loadingMessage = "Failed to load local AI"
            } catch (e: Error) {
                Log.e("AudioDebug", "Critical error", e)
                loadError = "OOM / Critical Error"
                transcription = "OOM / Critical Error"
                loadingMessage = "Not enough memory to load model"
            } finally {
                isLoadingAI = false
            }
        }
    }

    private fun updateLoadingState(rawProgress: String) {
        when {
            rawProgress.startsWith("1/3") -> {
                loadingMessage = "Preparing tokenizer..."
                loadingProgress = 0.33f
            }
            rawProgress.startsWith("2/3") -> {
                loadingMessage = "Loading decoder..."
                loadingProgress = 0.66f
            }
            rawProgress.startsWith("3/3") -> {
                loadingMessage = "Loading encoder..."
                loadingProgress = 0.95f
            }
            rawProgress == "AI Ready" -> {
                loadingMessage = "AI ready"
                loadingProgress = 1f
            }
            rawProgress.startsWith("Fail:") || rawProgress.startsWith("Error:") -> {
                loadingMessage = rawProgress
                loadError = rawProgress
            }
            else -> {
                loadingMessage = rawProgress
            }
        }
    }

    /**
     * Unified handler for all transcriptions (Mic or Asset)
     */
    private suspend fun handleTranscript(text: String) {
        withContext(Dispatchers.Main) {
            transcription = text.ifBlank { "(No speech detected)" }

            val announcement = AnnouncementProcessor.process(
                transcript = text,
                tripDetails = currentTrip
            )

            if (announcement != null) {
                Log.i(
                    "AudioDebug",
                    "MATCH FOUND: ${announcement.type} for ${announcement.matchedFlightNumber}"
                )
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
        // dont record before whsiper loads
        if (!isAILoaded) {
            transcription = "AI is still loading..."
            return
        }
        isRecording = true
        activeAlert = null // Reset alert when starting new listen
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
            activeAlert = null

            withContext(Dispatchers.Main) {
                transcription = "Running $fileName..."
            }

            val path = if (fileName == "test_audio.wav") {
                fileName
            } else {
                "audio/$fileName"
            }

            val result = whisperModel.transcribeFromAsset(path)
            handleTranscript(result)
        }
    }

    fun runLogicTest() {
        viewModelScope.launch {
            activeAlert = null

            val testTranscript =
                "Attention passengers, American Airlines flight 123 is now boarding at Gate 24B."

            transcription = testTranscript
            handleTranscript(testTranscript)
        }
    }

    fun clearAlert() {
        activeAlert = null
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
        private val tripDetails: TripDetails
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AudioDebugViewModel(application, tripDetails) as T
        }
    }
}

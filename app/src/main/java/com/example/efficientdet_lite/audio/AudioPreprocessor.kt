package com.example.efficientdet_lite.audio

import kotlin.math.*

/**
 * Optimized conversion of raw audio to Mel Spectrograms for Whisper.
 */
object AudioPreprocessor {
    private const val SAMPLE_RATE = 16000
    private const val N_FFT = 400
    private const val HOP_LENGTH = 160
    private const val N_MELS = 80
    private const val CHUNK_SIZE = 3000 // 30 seconds

    private val window = FloatArray(N_FFT) { 
        (0.5f * (1f - cos(2f * PI.toFloat() * it / N_FFT))) 
    }

    private val cosTable = FloatArray((N_FFT / 2 + 1) * N_FFT)
    private val sinTable = FloatArray((N_FFT / 2 + 1) * N_FFT)
    private val melFilters: Array<FloatArray> by lazy { createMelFilters() }

    init {
        for (k in 0..N_FFT / 2) {
            for (n in 0 until N_FFT) {
                val angle = 2.0 * PI * k * n / N_FFT
                cosTable[k * N_FFT + n] = cos(angle).toFloat()
                sinTable[k * N_FFT + n] = sin(angle).toFloat()
            }
        }
    }

    fun getMelSpectrogram(audioData: FloatArray): Array<FloatArray> {
        val melSpectrogram = Array(N_MELS) { FloatArray(CHUNK_SIZE) }
        val fixedAudio = FloatArray(SAMPLE_RATE * 30)
        audioData.copyInto(fixedAudio, 0, 0, minOf(audioData.size, fixedAudio.size))
        
        val magnitudes = FloatArray(N_FFT / 2 + 1)
        var globalMaxLogMel = -100f

        for (i in 0 until CHUNK_SIZE) {
            val start = i * HOP_LENGTH
            if (start + N_FFT > fixedAudio.size) break
            
            for (k in 0..N_FFT / 2) {
                var real = 0f
                var imag = 0f
                val offset = k * N_FFT
                for (n in 0 until N_FFT) {
                    val sample = fixedAudio[start + n] * window[n]
                    real += sample * cosTable[offset + n]
                    imag -= sample * sinTable[offset + n]
                }
                magnitudes[k] = real * real + imag * imag
            }
            
            for (m in 0 until N_MELS) {
                var melEnergy = 0f
                val filter = melFilters[m]
                for (k in 0..N_FFT / 2) {
                    melEnergy += magnitudes[k] * filter[k]
                }
                val logMel = log10(maxOf(melEnergy, 1e-10f))
                melSpectrogram[m][i] = logMel
                if (logMel > globalMaxLogMel) globalMaxLogMel = logMel
            }
        }
        
        // Standard Whisper Normalization: (logMel - max + 8) / 4 - 1
        // This makes the range roughly [-1.0, 1.0]
        for (m in 0 until N_MELS) {
            for (t in 0 until CHUNK_SIZE) {
                val valShifted = maxOf(melSpectrogram[m][t], globalMaxLogMel - 8.0f)
                melSpectrogram[m][t] = (valShifted + 4.0f) / 4.0f
            }
        }

        return melSpectrogram
    }

    private fun createMelFilters(): Array<FloatArray> {
        val filters = Array(N_MELS) { FloatArray(N_FFT / 2 + 1) }
        val minMel = 0f
        val maxMel = 2595f * log10(1f + (SAMPLE_RATE / 2f) / 700f)
        
        val melPoints = FloatArray(N_MELS + 2) { i ->
            minMel + (maxMel - minMel) * i / (N_MELS + 1)
        }
        val freqPoints = FloatArray(N_MELS + 2) { i ->
            700f * (10f.pow(melPoints[i] / 2595f) - 1f)
        }
        val binPoints = IntArray(N_MELS + 2) { i ->
            ((N_FFT + 1) * freqPoints[i] / SAMPLE_RATE).toInt()
        }
        
        for (m in 1..N_MELS) {
            val left = binPoints[m - 1]
            val center = binPoints[m]
            val right = binPoints[m + 1]
            val areaNorm = 2.0f / (freqPoints[m + 1] - freqPoints[m - 1])
            
            for (k in left until center) {
                filters[m - 1][k] = ((k - left).toFloat() / (center - left)) * areaNorm
            }
            for (k in center until right) {
                filters[m - 1][k] = ((right - k).toFloat() / (right - center)) * areaNorm
            }
        }
        return filters
    }
}

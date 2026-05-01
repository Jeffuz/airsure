package com.example.efficientdet_lite.audio

import android.content.Context
import android.util.Log
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.coroutines.coroutineContext

class WhisperModel(private val context: Context) {
    private var encoderModel: CompiledModel? = null
    private var decoderModel: CompiledModel? = null
    private var tokenizer: WhisperTokenizer? = null
    private val modelLock = Mutex()

    companion object {
        private const val TAG = "WhisperModel"
        private const val ENCODER_ASSET = "distil_whisper_encoder.tflite"
        private const val DECODER_ASSET = "distil_whisper_decoder.tflite"
        private const val MAX_TOKENS = 128
        private const val MEAN_DECODE_LEN = 200
        private const val MASK_NEG = -100f
    }

    var isLoaded = false
        private set

    suspend fun loadModels(onProgress: (String) -> Unit = {}) = withContext(Dispatchers.IO) {
        if (isLoaded) {
            withContext(Dispatchers.Main) { onProgress("AI Ready") }
            return@withContext
        }
        
        try {
            withContext(Dispatchers.Main) { onProgress("1/3: Tokenizer...") }
            tokenizer = WhisperTokenizer(context)
            System.gc()
            
            val options = CompiledModel.Options(Accelerator.CPU)
            
            // DIAGNOSTIC: Load Decoder FIRST to see if it's a file-specific issue or a RAM issue
            Log.d(TAG, "Step 2/3: Loading Decoder (290MB)...")
            withContext(Dispatchers.Main) { onProgress("2/3: Decoder (290MB)...") }
            
            decoderModel = CompiledModel.create(context.assets, DECODER_ASSET, options)
            Log.d(TAG, "Decoder loaded. Resting...")
            
            System.gc()
            kotlinx.coroutines.delay(2000) // Longer rest for the OS

            Log.d(TAG, "Step 3/3: Loading Encoder (370MB)...")
            withContext(Dispatchers.Main) { onProgress("3/3: Encoder (370MB)...") }
            
            encoderModel = CompiledModel.create(context.assets, ENCODER_ASSET, options)
            
            isLoaded = true
            withContext(Dispatchers.Main) { onProgress("AI Ready") }
            Log.d(TAG, "Success! 660MB AI models loaded.")
        } catch (e: Exception) {
            Log.e(TAG, "Load failed", e)
            withContext(Dispatchers.Main) { onProgress("Fail: ${e.message}") }
            close()
            throw e
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "CRITICAL OOM: Emulator needs more RAM (4GB+)")
            withContext(Dispatchers.Main) { onProgress("Error: Out of Memory") }
            close()
            throw e
        }
    }

    private fun logFloatStats(label: String, values: FloatArray) {
        var min = Float.MAX_VALUE
        var max = -Float.MAX_VALUE
        var sum = 0f
        var sumAbs = 0f

        for (v in values) {
            if (v < min) min = v
            if (v > max) max = v
            sum += v
            sumAbs += kotlin.math.abs(v)
        }

        Log.d(TAG, "$label size=${values.size}, min=$min, max=$max, mean=${sum / values.size}, meanAbs=${sumAbs / values.size}")
    }

    suspend fun transcribe(audioData: FloatArray): String = withContext(Dispatchers.Default) {
        modelLock.withLock {
            val encoder = encoderModel ?: return@withLock "Encoder not ready"
            val decoder = decoderModel ?: return@withLock "Decoder not ready"
            val tok = tokenizer ?: return@withLock "Tokenizer not ready"

            // Pre-allocate buffers once to avoid heavy memory pressure in the loop
            val encoderInputs = encoder.createInputBuffers()
            val encoderOutputs = encoder.createOutputBuffers()
            val dInputs = decoder.createInputBuffers()
            val dOutputs = decoder.createOutputBuffers()

            try {
                val mel = AudioPreprocessor.getMelSpectrogram(audioData)
                val melFlat = FloatArray(80 * 3000)
                for (m in 0 until 80) {
                    mel[m].copyInto(melFlat, m * 3000, 0, 3000)
                }
                logFloatStats("melFlat", melFlat)

                encoderInputs[0].writeFloat(melFlat)
                encoder.run(encoderInputs, encoderOutputs)

                val crossCaches = encoderOutputs.map { it.readFloat() }
                Log.d(TAG, "Encoder finished. Cross-caches collected: ${crossCaches.size}")

                val generatedTokens = mutableListOf<Int>()
                var currentToken = tok.startOfTranscript
                var selfCaches: List<FloatArray>? = null
                val attentionMask = FloatArray(MEAN_DECODE_LEN) { MASK_NEG }

                // Pre-fill cross-caches once as they don't change
                for (i in 0 until 8) {
                    dInputs[10 + i].writeFloat(crossCaches[i])
                }

                for (step in 0 until MAX_TOKENS) {
                    ensureActive()
                    attentionMask[MEAN_DECODE_LEN - 1 - step] = 0f

                    // 1. input_ids
                    dInputs[0].writeInt(intArrayOf(currentToken))

                    // 2. attention_mask
                    dInputs[1].writeFloat(attentionMask)

                    // 3. self-caches
                    for (i in 0 until 8) {
                        val input = dInputs[2 + i]
                        val cache = selfCaches?.get(i)
                        if (cache != null) {
                            input.writeFloat(cache)
                        } else {
                            // First step: write zeros. We assume size based on first run or model spec.
                            // The buffers are usually zeroed by LiteRT on creation.
                        }
                    }

                    // 5. position_ids
                    dInputs[18].writeInt(intArrayOf(step))

                    decoder.run(dInputs, dOutputs)

                    val logits = dOutputs[0].readFloat()
                    val nextToken = logits.indices.maxByOrNull { logits[it] } ?: tok.endOfText

                    if (step % 10 == 0) {
                        Log.d(TAG, "Step $step: token=$nextToken")
                    }

                    if (nextToken == tok.endOfText) {
                        break
                    }

                    selfCaches = dOutputs.drop(1).map { it.readFloat() }

                    if (!tok.isSpecialToken(nextToken)) {
                        generatedTokens.add(nextToken)
                    }
                    currentToken = nextToken
                }

                return@withLock tok.decode(generatedTokens)
            } catch (e: Exception) {
                Log.e(TAG, "Inference failed", e)
                return@withLock "Error: ${e.message}"
            } finally {
                encoderInputs.forEach { it.close() }
                encoderOutputs.forEach { it.close() }
                dInputs.forEach { it.close() }
                dOutputs.forEach { it.close() }
                System.gc()
            }
        }
    }

    suspend fun transcribeFromAsset(assetName: String): String {
        return try {
            val inputStream = withContext(Dispatchers.IO) {
                context.assets.open(assetName)
            }
            val bytes = inputStream.use { it.readBytes() }
            // Very simple WAV to FloatArray conversion (assuming 16kHz mono 16-bit)
            // Skip 44 byte header
            val shortBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
            val floatArray = FloatArray((shortBuffer.remaining() - 22).coerceAtLeast(0))
            for (i in 0 until floatArray.size) {
                if (i + 22 < shortBuffer.limit()) {
                    floatArray[i] = shortBuffer.get(i + 22) / 32768.0f
                }
            }
            transcribe(floatArray)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to transcribe from asset", e)
            "Error loading asset"
        }
    }


    fun close() {
        encoderModel?.close()
        decoderModel?.close()
    }
}

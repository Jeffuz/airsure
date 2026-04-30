package com.example.efficientdet_lite.audio

import android.content.Context
import android.util.Log
import com.google.ai.edge.litert.CompiledModel
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WhisperModel(private val context: Context) {
    private var encoderModel: CompiledModel? = null
    private var decoderModel: CompiledModel? = null
    private val tokenizer = WhisperTokenizer(context)

    companion object {
        private const val TAG = "WhisperModel"
        private const val ENCODER_ASSET = "distil_whisper_encoder.tflite"
        private const val DECODER_ASSET = "distil_whisper_decoder.tflite"
        private const val MAX_TOKENS = 128
        private const val MEAN_DECODE_LEN = 200
        private const val MASK_NEG = -100f
    }

    init {
        loadModels()
    }

    private fun loadModels() {
        runCatching {
            encoderModel = CompiledModel.create(context.assets, ENCODER_ASSET)
            decoderModel = CompiledModel.create(context.assets, DECODER_ASSET)
            Log.d(TAG, "Models loaded successfully")
        }.onFailure { e ->
            Log.e(TAG, "Error loading models", e)
        }
    }

    private fun dumpBuffers(label: String, buffers: List<com.google.ai.edge.litert.TensorBuffer>) {
        buffers.forEachIndexed { index, buffer ->
            val floatSize = runCatching { buffer.readFloat().size }.getOrDefault(-1)
            val int8Size = runCatching { buffer.readInt8().size }.getOrDefault(-1)
            val int32Size = runCatching { buffer.readInt().size }.getOrDefault(-1)
            Log.d(TAG, "$label[$index] floatSize=$floatSize int8Size=$int8Size int32Size=$int32Size")
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

    fun transcribe(audioData: FloatArray): String {
        val encoder = encoderModel ?: return "Encoder not ready"
        val decoder = decoderModel ?: return "Decoder not ready"

        try {
            val mel = AudioPreprocessor.getMelSpectrogram(audioData)
            val melFlat = FloatArray(80 * 3000) 
            for (m in 0 until 80) {
                mel[m].copyInto(melFlat, m * 3000, 0, 3000)
            }
            logFloatStats("melFlat", melFlat)

            val encoderInputs = encoder.createInputBuffers()
            val encoderOutputs = encoder.createOutputBuffers()
            
            encoderInputs[0].writeFloat(melFlat)
            encoder.run(encoderInputs, encoderOutputs)
            
            val crossCaches = encoderOutputs.map { it.readFloat() }
            Log.d(TAG, "Encoder finished. Cross-caches collected: ${crossCaches.size}")

            val generatedTokens = mutableListOf<Int>()
            var currentToken = tokenizer.startOfTranscript
            var selfCaches: List<FloatArray>? = null
            val attentionMask = FloatArray(MEAN_DECODE_LEN) { MASK_NEG }

            for (step in 0 until MAX_TOKENS) {
                attentionMask[MEAN_DECODE_LEN - 1 - step] = 0f

                val dInputs = decoder.createInputBuffers()
                val dOutputs = decoder.createOutputBuffers()

                if (step == 0) {
                    dumpBuffers("DECODER INPUT", dInputs)
                    dumpBuffers("DECODER OUTPUT", dOutputs)
                }

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
                        input.writeFloat(FloatArray(input.readFloat().size))
                    }
                }

                // 4. cross-caches
                for (i in 0 until 8) {
                    dInputs[10 + i].writeFloat(crossCaches[i])
                }

                // 5. position_ids
                dInputs[18].writeInt(intArrayOf(step))

                decoder.run(dInputs, dOutputs)

                val logits = dOutputs[0].readFloat()
                val nextToken = logits.indices.maxByOrNull { logits[it] } ?: tokenizer.endOfText

                Log.d(TAG, "Step $step: token=$nextToken text='${tokenizer.decode(listOf(nextToken))}'")

                selfCaches = dOutputs.drop(1).map { it.readFloat() }

                dInputs.forEach { it.close() }
                dOutputs.forEach { it.close() }

                if (nextToken == tokenizer.endOfText) {
                    break
                }

                if (!tokenizer.isSpecialToken(nextToken)) {
                    generatedTokens.add(nextToken)
                }
                currentToken = nextToken
            }
            
            encoderInputs.forEach { it.close() }
            encoderOutputs.forEach { it.close() }

            return tokenizer.decode(generatedTokens)
        } catch (e: Exception) {
            Log.e(TAG, "Inference failed", e)
            return "Error: ${e.message}"
        }
    }

    fun transcribeFromAsset(assetName: String): String {
        return try {
            val inputStream = context.assets.open(assetName)
            val bytes = inputStream.readBytes()
            // Very simple WAV to FloatArray conversion (assuming 16kHz mono 16-bit)
            // Skip 44 byte header
            val shortBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
            val floatArray = FloatArray(shortBuffer.remaining() - 22) // ~44 bytes
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

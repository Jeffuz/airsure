package com.example.efficientdet_lite.audio

import android.content.Context
import org.json.JSONObject

/**
 * A simple tokenizer for Whisper models that maps IDs to strings.
 * Note: A full implementation for production would include BPE (Byte Pair Encoding).
 * This class handles the ID -> Word mapping for decoding results.
 */
class WhisperTokenizer(private val context: Context) {
    private val idToToken = mutableMapOf<Int, String>()
    
    // Special tokens for Whisper Small English
    val startOfTranscript = 50258
    val en = 50259
    val transcribe = 50262
    val endOfText = 50257
    val noSpeech = 50362

    init {
        loadVocab()
    }

    private fun loadVocab() {
        runCatching {
            val jsonString = context.assets.open("vocab.json").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            jsonObject.keys().forEach { token ->
                val id = jsonObject.getInt(token)
                idToToken[id] = token
            }
        }.onFailure {
            // Fallback or log error
        }
    }

    fun decode(ids: List<Int>): String {
        return ids.mapNotNull { idToToken[it] }
            .joinToString("")
            .replace("Ġ", " ") // Whisper uses Ġ as a space marker
            .replace("<|endoftext|>", "")
            .trim()
    }
}

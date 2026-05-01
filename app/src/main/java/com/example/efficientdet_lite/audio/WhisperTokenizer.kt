package com.example.efficientdet_lite.audio

import android.content.Context
import android.util.Log
import org.json.JSONObject

class WhisperTokenizer(private val context: Context) {
    private val idToToken = mutableMapOf<Int, String>()
    private val tokenToId = mutableMapOf<String, Int>()

    val endOfText: Int
    val startOfTranscript: Int
    val noTimestamps: Int

    init {
        loadVocab()

        val config = loadJsonOrNull("config.json")

        // English-only Whisper / Distil-Whisper usually:
        // decoder_start_token_id = 50257
        // eos_token_id = 50256
        startOfTranscript = config?.optInt("decoder_start_token_id", 50257) ?: 50257
        endOfText = config?.optInt("eos_token_id", 50256) ?: 50256
        noTimestamps = tokenToId["<|notimestamps|>"] ?: 50362

        Log.d("WhisperTokenizer", "SOT=$startOfTranscript")
        Log.d("WhisperTokenizer", "EOT=$endOfText")
        Log.d("WhisperTokenizer", "NO_TIMESTAMPS=$noTimestamps")
        Log.d("WhisperTokenizer", "VOCAB_SIZE=${idToToken.size}")
    }

    private fun loadVocab() {
        val jsonString = context.assets.open("vocab.json")
            .bufferedReader()
            .use { it.readText() }

        val jsonObject = JSONObject(jsonString)

        jsonObject.keys().forEach { token ->
            val id = jsonObject.getInt(token)
            tokenToId[token] = id
            idToToken[id] = token
        }
    }

    private fun loadJsonOrNull(assetName: String): JSONObject? {
        return runCatching {
            val jsonString = context.assets.open(assetName)
                .bufferedReader()
                .use { it.readText() }
            JSONObject(jsonString)
        }.getOrNull()
    }

    fun isSpecialToken(id: Int): Boolean {
        return id >= 50256
    }

    fun decode(ids: List<Int>): String {
        return ids
            .filter { !isSpecialToken(it) }
            .mapNotNull { idToToken[it] }
            .joinToString("")
            .replace("Ġ", " ")
            .replace("Ċ", "\n")
            .trim()
    }
}

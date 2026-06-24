package com.example.ui.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class VoiceManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e("VoiceManager", "Error initializing TTS: ${e.message}")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("es", "MX")) ?: TextToSpeech.LANG_NOT_SUPPORTED
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to Spanish generic
                tts?.setLanguage(Locale("es"))
            }
            isInitialized = true
            Log.d("VoiceManager", "TTS Initialized Successfully")
        } else {
            Log.e("VoiceManager", "TTS Initialization Failed")
        }
    }

    fun speak(text: String) {
        if (isInitialized && tts != null) {
            // Clean up Markdown or raw formula tags before speaking so it sounds natural
            val cleanText = text
                .replace("*", "")
                .replace("#", "")
                .replace("=", "igual a")
                .replace("psi", "pesei")
                .replace("ppg", "pepege")
                .replace("ft", "pies")
                .replace("bbl", "barriles")
                .replace("/", " por ")
                .replace("ID", "diámetro interno")
                .replace("OD", "diámetro externo")
            
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "PetroCalcTTS")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.model.VoiceCommandResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class AuraVoiceManager(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _lastVoiceResult = MutableStateFlow<VoiceCommandResult?>(null)
    val lastVoiceResult: StateFlow<VoiceCommandResult?> = _lastVoiceResult.asStateFlow()

    private val _speechError = MutableStateFlow<String?>(null)
    val speechError: StateFlow<String?> = _speechError.asStateFlow()

    fun isSpeechRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening() {
        if (!isSpeechRecognitionAvailable()) {
            _speechError.value = "Speech recognition is not available on this device. Use text search fallback."
            return
        }

        try {
            stopListening()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                    _speechError.value = null
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    _isListening.value = false
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    val errorMsg = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                        SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try again."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition engine busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                        else -> "Speech recognition error ($error)"
                    }
                    _speechError.value = errorMsg
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val recognized = matches?.firstOrNull()?.trim()
                    if (!recognized.isNullOrEmpty()) {
                        processVoiceCommand(recognized)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak a command (e.g. 'Play', 'Next', 'Search Starlight')")
            }

            recognizer.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            _speechError.value = "Unable to start speech recognizer: ${e.message}"
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        _isListening.value = false
    }

    fun processVoiceCommand(input: String): VoiceCommandResult {
        val clean = input.lowercase().trim()
        val result = when {
            clean == "play" || clean == "resume" || clean == "start music" -> VoiceCommandResult.Play
            clean == "pause" || clean == "stop" || clean == "halt" -> VoiceCommandResult.Pause
            clean == "next" || clean == "skip" || clean == "next track" || clean == "next song" -> VoiceCommandResult.NextTrack
            clean == "previous" || clean == "back" || clean == "prev" || clean == "prev track" -> VoiceCommandResult.PreviousTrack
            clean.contains("favorite") || clean.contains("like") || clean.contains("heart") -> VoiceCommandResult.FavoriteCurrent
            clean.contains("library") || clean.contains("my music") || clean.contains("open library") -> VoiceCommandResult.OpenLibrary
            clean.startsWith("search ") || clean.startsWith("find ") || clean.startsWith("play ") -> {
                val query = clean.removePrefix("search ").removePrefix("find ").removePrefix("play ").trim()
                if (query.isNotEmpty()) VoiceCommandResult.SearchQuery(query) else VoiceCommandResult.Play
            }
            else -> {
                if (clean.length > 2) VoiceCommandResult.SearchQuery(clean) else VoiceCommandResult.Unknown(input)
            }
        }

        _lastVoiceResult.value = result
        return result
    }

    fun clearLastResult() {
        _lastVoiceResult.value = null
        _speechError.value = null
    }
}

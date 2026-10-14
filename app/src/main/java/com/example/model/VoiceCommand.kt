package com.example.model

sealed interface VoiceCommandResult {
    data object Play : VoiceCommandResult
    data object Pause : VoiceCommandResult
    data object NextTrack : VoiceCommandResult
    data object PreviousTrack : VoiceCommandResult
    data object FavoriteCurrent : VoiceCommandResult
    data object OpenLibrary : VoiceCommandResult
    data class SearchQuery(val query: String) : VoiceCommandResult
    data class Unknown(val rawText: String) : VoiceCommandResult
}

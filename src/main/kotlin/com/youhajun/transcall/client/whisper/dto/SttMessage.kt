package com.youhajun.transcall.client.whisper.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class SttMessage(
    val status: String = "",
    val lines: List<SttLine> = emptyList(),
    @JsonProperty("buffer_transcription")
    val bufferTranscription: String?,
    @JsonProperty("buffer_diarization")
    val bufferDiarization: String?,
    @JsonProperty("remaining_time_transcription")
    val remainingTimeTranscription: Double?,
    @JsonProperty("remaining_time_diarization")
    val remainingTimeDiarization: Double?
): WhisperEvent

data class SttLine(
    val speaker: Int,
    val text: String,
    val start: String,
    val end: String,
    @JsonProperty("detected_language")
    val detectedLanguage: String? = null
)
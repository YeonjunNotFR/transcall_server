package com.youhajun.transcall.whisper.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TranscriptUpdate(
    val type: String = "transcript_update",
    val status: String,
    val segments: List<Segment>,
    val metadata: Metadata
) : WhisperEvent

data class Segment(
    val id: Int,
    val speaker: Int,
    val text: String,
    @JsonProperty("start_speaker")
    val startSpeaker: Double,
    val start: Double,
    val end: Double,
    val language: String? = null,
    val words: List<Word> = emptyList(),
    val buffer: Buffer = Buffer(),
    @JsonProperty("is_finalized")
    val isFinal: Boolean
)

data class Word(
    val text: String,
    val start: Double,
    val end: Double,
    val validated: Validated = Validated()
)

data class Validated(
    val text: Boolean = false,
    val speaker: Boolean = false
)

data class Buffer(
    val transcription: String = "",
    val diarization: String = "",
    val translation: String = ""
)

data class Metadata(
    @JsonProperty("remaining_time_transcription")
    val remainingTimeTranscription: Double? = null,
    @JsonProperty("remaining_time_diarization")
    val remainingTimeDiarization: Double? = null
)
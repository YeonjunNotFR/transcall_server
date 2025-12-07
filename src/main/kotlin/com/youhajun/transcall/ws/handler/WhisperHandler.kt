package com.youhajun.transcall.ws.handler

import com.youhajun.transcall.whisper.TranscriptSegmentAssembler
import com.youhajun.transcall.whisper.dto.TranscriptUpdate
import com.youhajun.transcall.whisper.dto.WhisperEvent
import com.youhajun.transcall.whisper.ws.WhisperWebSocketClient
import com.youhajun.transcall.ws.sendBinaryMessage
import com.youhajun.transcall.ws.session.RoomSessionManager
import com.youhajun.transcall.ws.vo.WhisperSessionInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import java.util.*

@Component
class WhisperHandler(
    private val roomSessionManager: RoomSessionManager,
    private val whisperWebSocketClient: WhisperWebSocketClient,
    private val segmentAssembler: TranscriptSegmentAssembler
) {

    private val logger: Logger = LogManager.getLogger(WhisperHandler::class.java)

    suspend fun connectWhisper(roomId: UUID, userId: UUID): WebSocketSession {
        val session = roomSessionManager.requireContext(roomId, userId)
        val whisperConnection = whisperWebSocketClient.connect(session.language)
        val runtimeJob = SupervisorJob()

        val whisperSessionInfo = WhisperSessionInfo(
            whisperSession = whisperConnection.session,
            runtimeJob = runtimeJob,
        )

        roomSessionManager.updateUserSession(roomId, userId) {
            it.copy(whisperSessionInfo = whisperSessionInfo)
        }

        return whisperConnection.session.also {
            CoroutineScope(Dispatchers.IO + runtimeJob).launch {
                observeWhisperEvent(roomId, userId, whisperConnection.eventFlow)
            }
        }
    }

    suspend fun sendAudioData(roomId: UUID, userId: UUID, data: ByteArray) {
        val session = roomSessionManager.requireWhisperSession(roomId, userId)
        session.whisperSession.sendBinaryMessage(data)
    }

    suspend fun disposeWhisper(roomId: UUID, userId: UUID) {
        logger.info("Disconnecting Whisper")
        val whisperSessionInfo = roomSessionManager.getUserSession(roomId, userId)?.whisperSessionInfo ?: return
        whisperSessionInfo.runtimeJob.cancelAndJoin()
        val whisperSession = whisperSessionInfo.whisperSession
        if (whisperSession.isOpen) whisperSession.close().awaitSingleOrNull()
    }

    private suspend fun observeWhisperEvent(roomId: UUID, userId: UUID, event: SharedFlow<WhisperEvent>) {
        event.filterIsInstance<TranscriptUpdate>()
            .map { it.segments.filter { it.speaker == SILENCE_SPEAKER } }
            .conflate()
            .collect { segments ->
                if(segments.isEmpty()) return@collect

                segments.forEach {
                    segmentAssembler.onUpdate(roomId, userId, it)
                }
            }
    }

    companion object {
        private const val SILENCE_SPEAKER = -2
    }
}
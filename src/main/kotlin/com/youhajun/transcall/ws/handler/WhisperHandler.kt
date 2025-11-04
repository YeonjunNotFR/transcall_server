package com.youhajun.transcall.ws.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.whisper.dto.TranscriptUpdate
import com.youhajun.transcall.whisper.dto.WhisperEvent
import com.youhajun.transcall.whisper.ws.WhisperWebSocketClient
import com.youhajun.transcall.user.service.UserService
import com.youhajun.transcall.whisper.TranscriptSegmentAssembler
import com.youhajun.transcall.ws.exception.WebSocketException
import com.youhajun.transcall.ws.session.RoomSessionManager
import com.youhajun.transcall.ws.vo.WhisperSessionInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import java.util.*
import kotlin.coroutines.coroutineContext

@Component
class WhisperHandler(
    private val objectMapper: ObjectMapper,
    private val roomSessionManager: RoomSessionManager,
    private val whisperWebSocketClient: WhisperWebSocketClient,
    private val userService: UserService,
    private val segmentAssembler: TranscriptSegmentAssembler
) {

    private val logger: Logger = LogManager.getLogger(WhisperHandler::class.java)

    suspend fun connectWhisper(roomId: UUID, userId: UUID): WebSocketSession {
        val session = roomSessionManager.getUserSession(roomId, userId) ?: throw WebSocketException.SessionNotFound()
        val whisperConnection = whisperWebSocketClient.connect(session.language)
        val runtimeJob = SupervisorJob()

        val whisperSessionInfo = WhisperSessionInfo(
            whisperSession = whisperConnection.session,
            runtimeJob = runtimeJob,
        )

        roomSessionManager.updateUserSession(roomId, userId) {
            it.copy(whisperSessionInfo = whisperSessionInfo)
        }

        val scope = CoroutineScope(coroutineContext + runtimeJob)

        scope.launch {
            observeWhisperEvent(roomId, userId, whisperConnection.eventFlow)
        }

        return whisperConnection.session
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
package com.youhajun.transcall.ws.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.call.conversation.domain.ConversationCache
import com.youhajun.transcall.call.conversation.service.CallConversationService
import com.youhajun.transcall.client.whisper.dto.SttMessage
import com.youhajun.transcall.client.whisper.dto.WhisperEvent
import com.youhajun.transcall.client.whisper.ws.WhisperWebSocketClient
import com.youhajun.transcall.user.domain.LanguageType
import com.youhajun.transcall.user.service.UserService
import com.youhajun.transcall.ws.dto.ServerMessage
import com.youhajun.transcall.ws.dto.payload.TranslationMessage
import com.youhajun.transcall.ws.exception.WebSocketException
import com.youhajun.transcall.ws.sendServerMessage
import com.youhajun.transcall.ws.session.RoomSessionManager
import com.youhajun.transcall.ws.vo.MessageType
import com.youhajun.transcall.ws.vo.WhisperSessionInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.SharedFlow
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
    private val callConversationService: CallConversationService,
) {

    private val logger: Logger = LogManager.getLogger(WhisperHandler::class.java)

    suspend fun connectWhisper(roomId: UUID, userId: UUID): WebSocketSession {
        val language = userService.getMyInfo(userId).languageCode
        val whisperConnection = whisperWebSocketClient.connect(language)
        val runtimeJob = SupervisorJob()

        val whisperSessionInfo = WhisperSessionInfo(
            whisperSession = whisperConnection.session,
            runtimeJob = runtimeJob,
            language = language
        )

        roomSessionManager.updateUserSession(roomId, userId) {
            it.copy(whisperSessionInfo = whisperSessionInfo)
        }

        val scope = CoroutineScope(coroutineContext + runtimeJob)

        scope.launch {
            observeWhisperEvent(roomId, userId, whisperConnection.eventFlow)
        }

        scope.launch {
            observeConversationCache(roomId, userId)
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

    private suspend fun observeConversationCache(roomId: UUID, userId: UUID) {
        val userSession = roomSessionManager.getUserSession(roomId, userId)?.userSession ?: throw WebSocketException.SessionNotFound()
        callConversationService.subscribeConversationCache(roomId, userId).collect {
            logger.info("WhisperHandler - observeConversationCache: cache=$it")
            sendTranslationMessage(roomId, userId, userSession, it)
        }
    }

    private suspend fun observeWhisperEvent(roomId: UUID, userId: UUID, event: SharedFlow<WhisperEvent>) {
        val language = roomSessionManager.getUserSession(roomId, userId)?.whisperSessionInfo?.language ?: return
        event.collect {
            when (it) {
                is SttMessage -> handleSttMessage(roomId, userId, it, language)
            }
        }
    }

    private suspend fun handleSttMessage(roomId: UUID, userId: UUID, msg: SttMessage, language: LanguageType) {
        val lastLine = msg.lines.lastOrNull() ?: return
        if(lastLine.text.isBlank()) return

        val cache = ConversationCache(
            originText = lastLine.text,
            originLanguage = language,
            start = lastLine.start
        )
        callConversationService.publishConversationCache(roomId, userId, cache)
    }

    private suspend fun sendTranslationMessage(roomId: UUID, userId: UUID, userSession: WebSocketSession, cache: ConversationCache) {
        val payload = TranslationMessage(
            conversationId = cache.uuid.toString(),
            roomId = roomId.toString(),
            senderId = userId.toString(),
            originText = cache.originText,
            originLanguage = cache.originLanguage,
            createdAtToEpochTime = cache.timestamp,
        )
        val message = ServerMessage(type = MessageType.TRANSLATION, payload = payload)
        userSession.sendServerMessage(message, objectMapper)
    }
}
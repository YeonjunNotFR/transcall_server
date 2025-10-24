package com.youhajun.transcall.ws.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.janus.dto.event.*
import com.youhajun.transcall.janus.dto.plugin.JanusPlugin
import com.youhajun.transcall.janus.dto.plugin.TrickleCandidateBody
import com.youhajun.transcall.janus.dto.video.VideoRoomJsep
import com.youhajun.transcall.janus.service.JanusPluginService
import com.youhajun.transcall.janus.service.JanusSessionService
import com.youhajun.transcall.janus.ws.JanusWebSocketClient
import com.youhajun.transcall.ws.dto.ServerMessage
import com.youhajun.transcall.ws.dto.payload.*
import com.youhajun.transcall.ws.dto.toPublisherFeedResponse
import com.youhajun.transcall.ws.exception.WebSocketException
import com.youhajun.transcall.ws.sendServerMessage
import com.youhajun.transcall.ws.session.RoomSessionManager
import com.youhajun.transcall.ws.vo.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import java.util.*
import kotlin.coroutines.coroutineContext

@Component
class JanusHandler(
    private val objectMapper: ObjectMapper,
    private val roomSessionManager: RoomSessionManager,
    private val janusWebSocketClient: JanusWebSocketClient,
    private val janusSessionService: JanusSessionService,
    private val janusPluginService: JanusPluginService,
) {
    companion object {
        private const val KEEP_ALIVE_INTERVAL_MS = 30000L
    }

    private val logger: Logger = LogManager.getLogger(JanusHandler::class.java)

    suspend fun connectJanus(roomId: UUID, userId: UUID): WebSocketSession {
        val janusConnection = janusWebSocketClient.connect()
        val janusSessionId = janusSessionService.createSession(janusConnection.session).getOrThrow().sessionId
        val videoRoomHandleInfo = getVideoRoomHandleInfo(janusConnection.session, janusSessionId)
        val runtimeJob = SupervisorJob()

        val janusSessionInfo = JanusSessionInfo(
            janusSession = janusConnection.session,
            janusSessionId = janusSessionId,
            videoRoomHandleInfo = videoRoomHandleInfo,
            runtimeJob = runtimeJob
        )

        roomSessionManager.updateUserSession(roomId, userId) {
            it.copy(janusSessionInfo = janusSessionInfo)
        }

        val scope = CoroutineScope(coroutineContext + runtimeJob)

        scope.launch {
            observeJanusEvent(roomId, userId, janusConnection.eventFlow)
        }

        scope.launch {
            janusKeepAlive(janusConnection.session, janusSessionId)
        }

        return janusConnection.session
    }

    suspend fun disposeJanus(roomId: UUID, userId: UUID) {
        val janusSessionInfo = roomSessionManager.getUserSession(roomId, userId)?.janusSessionInfo ?: return
        janusSessionInfo.runtimeJob.cancelAndJoin()
        val janusSession = janusSessionInfo.janusSession
        janusSessionService.destroySession(janusSession, janusSessionInfo.janusSessionId)
        if (janusSession.isOpen) janusSession.close().awaitSingleOrNull()
    }

    private suspend fun janusKeepAlive(janusSession: WebSocketSession, janusSessionId: Long) {
        while (janusSession.isOpen && currentCoroutineContext().isActive) {
            try {
                janusSessionService.keepAlive(janusSession, janusSessionId)
                delay(KEEP_ALIVE_INTERVAL_MS)
            } catch (e: Exception) {
                if(e !is CancellationException) logger.warn("Keep alive failed: ${e.message}")
                break
            }
        }
    }

    private suspend fun observeJanusEvent(roomId: UUID, userId: UUID, event: SharedFlow<JanusEvent>) {
        event.collect {
            when (it) {
                is TrickleCandidateResponse<*> -> it.trickleCandidateHandle(roomId, userId)
                is JanusMedia -> it.janusMediaHandle(roomId, userId)
                is JanusPluginEvent<*> -> {
                    when (val eventData = it.pluginData.data) {
                        is OnNewPublisher -> eventData.newPublisherEventHandle(roomId, userId, it.jsep)
                    }
                }
            }
        }
    }

    private suspend fun TrickleCandidateResponse<*>.trickleCandidateHandle(roomId: UUID, userId: UUID) {
        if (candidate !is TrickleCandidateBody) return

        val participant = roomSessionManager.getUserSession(roomId, userId) ?: throw WebSocketException.SessionNotFound()
        val payload = OnIceCandidate(
            candidate = candidate.candidate,
            sdpMid = candidate.sdpMid,
            sdpMLineIndex = candidate.sdpMLineIndex,
            handleId = handleId
        )
        val message = ServerMessage(type = MessageType.SIGNALING, payload = payload)
        participant.userSession.sendServerMessage(message, objectMapper)
    }

    private suspend fun JanusMedia.janusMediaHandle(roomId: UUID, userId: UUID) {
        val participant = roomSessionManager.getUserSession(roomId, userId)
        val janusSessionInfo = participant?.janusSessionInfo ?: throw WebSocketException.SessionNotFound()

        val publisherInfo = janusSessionInfo.publisherInfoMap[handleId]
        val isAudioType = MediaType.fromType(type) == MediaType.AUDIO

        if (publisherInfo?.mediaContentType == MediaContentType.DEFAULT && isAudioType) {
            val userSession = participant.userSession
            sendSttStart(userSession)
            sendMediaStateInit(roomId, userId, userSession)
        }
    }

    private suspend fun OnNewPublisher.newPublisherEventHandle(roomId: UUID, userId: UUID, jsep: VideoRoomJsep?) {
        val participant = roomSessionManager.getUserSession(roomId, userId) ?: throw WebSocketException.SessionNotFound()
        val userSession = participant.userSession
        val payload = NewPublishers(feeds = publishers.map { it.toPublisherFeedResponse() })
        val message = ServerMessage(type = MessageType.SIGNALING, payload = payload)
        userSession.sendServerMessage(message, objectMapper)
    }

    private suspend fun getVideoRoomHandleInfo(
        janusSession: WebSocketSession,
        janusSessionId: Long
    ): VideoRoomHandleInfo {
        return coroutineScope {
            val defaultPub = async {
                janusPluginService.attachPlugin(janusSession, janusSessionId, JanusPlugin.VIDEO_ROOM).getOrThrow().handleId
            }
            val screenSharePub = async {
                janusPluginService.attachPlugin(janusSession, janusSessionId, JanusPlugin.VIDEO_ROOM).getOrThrow().handleId
            }
            val subscriber = async {
                janusPluginService.attachPlugin(janusSession, janusSessionId, JanusPlugin.VIDEO_ROOM).getOrThrow().handleId
            }

            VideoRoomHandleInfo(
                defaultPublisherHandleId = defaultPub.await(),
                screenSharePublisherHandleId = screenSharePub.await(),
                subscriberHandleId = subscriber.await()
            )
        }
    }

    private suspend fun sendSttStart(userSession: WebSocketSession) {
        val message = ServerMessage(type = MessageType.TRANSLATION, payload = SttStart)
        userSession.sendServerMessage(message, objectMapper)
    }

    private suspend fun sendMediaStateInit(roomId: UUID, userId: UUID, userSession: WebSocketSession) {
        val mediaStateList = roomSessionManager.getUsersSession(roomId)
            .map { it.toMediaStateDto() }
            .filterNot { it.userId == userId.toString() }
        val payload = MediaStateInit(mediaStateList = mediaStateList)
        val message = ServerMessage(type = MessageType.MEDIA_STATE, payload = payload)
        userSession.sendServerMessage(message, objectMapper)
    }
}
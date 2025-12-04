package com.youhajun.transcall.ws.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.youhajun.transcall.janus.dto.JanusPlugin
import com.youhajun.transcall.janus.service.JanusService
import com.youhajun.transcall.janus.service.JanusSignalingService
import com.youhajun.transcall.janus.JanusWebSocketClient
import com.youhajun.transcall.janus.dto.*
import com.youhajun.transcall.ws.dto.ServerMessage
import com.youhajun.transcall.ws.dto.payload.*
import com.youhajun.transcall.ws.sendServerMessage
import com.youhajun.transcall.ws.session.RoomSessionManager
import com.youhajun.transcall.ws.vo.JanusSessionInfo
import com.youhajun.transcall.ws.vo.MediaContentType
import com.youhajun.transcall.ws.vo.MessageType
import com.youhajun.transcall.ws.vo.VideoRoomHandleInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Component
import java.util.*

@Component
class JanusHandler(
    private val roomSessionManager: RoomSessionManager,
    private val janusService: JanusService,
    private val janusSignalingService: JanusSignalingService,
    private val janusWebSocketClient: JanusWebSocketClient,
    private val objectMapper: ObjectMapper
) {
    private val logger: Logger = LogManager.getLogger(JanusHandler::class.java)

    suspend fun setupJanus(roomId: UUID, userId: UUID) {
        logger.info("Setting up Janus for $userId")
        val session = janusWebSocketClient.connect { response ->
            CoroutineScope(Dispatchers.IO).launch {
                handleJanusAsyncEvent(roomId, userId, response)
            }
        }

        val sessionId = janusService.createSession(session).getOrThrow()

        val handleInfo = coroutineScope {
            val pubDefault = async { janusService.attachPlugin(session, sessionId, JanusPlugin.VIDEO_ROOM.pkgName).getOrThrow() }
            val pubScreen = async { janusService.attachPlugin(session, sessionId, JanusPlugin.VIDEO_ROOM.pkgName).getOrThrow() }
            val sub = async { janusService.attachPlugin(session, sessionId, JanusPlugin.VIDEO_ROOM.pkgName).getOrThrow() }

            VideoRoomHandleInfo(
                defaultPublisherHandleId = pubDefault.await(),
                screenSharePublisherHandleId = pubScreen.await(),
                subscriberHandleId = sub.await()
            )
        }

        val runtimeJob = SupervisorJob()

        val janusSession = JanusSessionInfo(
            session = session,
            sessionId = sessionId,
            handleInfo = handleInfo,
            runtimeJob = runtimeJob
        )
        roomSessionManager.updateUserSession(roomId, userId) { it.copy(janusSessionInfo = janusSession) }
        janusService.startKeepAlive(janusSession, CoroutineScope(Dispatchers.IO + runtimeJob))
    }

    private suspend fun handleJanusAsyncEvent(roomId: UUID, userId: UUID, response: JanusResponse) {
        when (response) {
            is JanusEventResponse -> {
                val data = response.plugindata?.data as? VideoRoomData.Event ?: return
                processVideoRoomEvent(roomId, userId, data)
            }

            is JanusTrickleResponse -> {
                val handleInfo = roomSessionManager.requireJanusSession(roomId, userId).handleInfo

                when(response.candidate) {
                    is JanusIceCandidate -> {
                        val handleId = response.sender ?: return
                        val (isPublisher, mediaContentType) = handleInfo.identifyHandle(handleId) ?: return
                        val payload = SignalingIceCandidate(
                            isPublisher = isPublisher,
                            mediaContentType = mediaContentType,
                            janusCandidate = response.candidate,
                        )
                        sendToClient(roomId, userId, MessageType.SIGNALING, payload)
                    }
                    is JanusTrickleCompleted -> Unit
                }
            }

            is JanusMediaResponse -> {
                if (response.receiving && response.type == "audio") {
                    sendSttStart(roomId, userId)
                    sendMediaStateInit(roomId, userId, )
                }
            }

            is JanusErrorResponse -> {
                logger.error("[Janus Error] Session: ${response.sessionId}, Reason: ${response.error.reason}")
            }

            else -> logger.debug("[Janus Unknown] Type: ${response.janus}")
        }
    }

    private suspend fun processVideoRoomEvent(roomId: UUID, userId: UUID, data: VideoRoomData.Event) {
        data.publishers?.takeIf { it.isNotEmpty() }?.let {
            sendToClient(roomId, userId, MessageType.SIGNALING, NewPublisherEvent(MediaContentType.DEFAULT, it))
        }
        (data.leaving as? Long? ?: data.unpublished)?.let {
            sendToClient(roomId, userId, MessageType.SIGNALING, PublisherUnpublished(it))
        }
    }

    private suspend fun sendSttStart(roomId: UUID, userId: UUID) {
        sendToClient(roomId, userId, MessageType.TRANSLATION, SttStart)
    }

    private suspend fun sendMediaStateInit(roomId: UUID, userId: UUID) {
        val mediaStateList = roomSessionManager.getUsersSessionInRoom(roomId)
            .filterNot { it.userId == userId }
            .map { it.toMediaStateDto() }
        val payload = MediaStateInit(mediaStateList = mediaStateList)
        sendToClient(roomId, userId, MessageType.MEDIA_STATE, payload)
    }

    private suspend fun sendToClient(roomId: UUID, userId: UUID, messageType: MessageType, payload: ResponsePayload) {
        runCatching {
            val userContext = roomSessionManager.requireContext(roomId, userId)
            val serverMessage = ServerMessage(type = messageType, payload = payload)
            userContext.userSession.sendServerMessage(serverMessage, objectMapper)
        }.onFailure { e ->
            logger.error("[Push Failed] User: $userId, Error: ${e.message}")
        }
    }

    suspend fun disposeJanus(roomId: UUID, userId: UUID) {
        logger.info("Dispose Janus for $userId")
        roomSessionManager.getUserSession(roomId, userId)?.janusSessionInfo?.let {
            janusSignalingService.leave(it)
            it.runtimeJob.cancel()
            janusService.destroySession(it.session, it.sessionId)
            if (it.session.isOpen) it.session.close().awaitSingleOrNull()
        }
    }
}
package com.youhajun.transcall.whisper

import com.youhajun.transcall.call.conversation.service.CallConversationService
import com.youhajun.transcall.whisper.dto.Segment
import com.youhajun.transcall.ws.dto.ServerMessage
import com.youhajun.transcall.ws.session.RoomSessionManager
import com.youhajun.transcall.ws.vo.MessageType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class TranscriptSegmentAssembler(
    private val conversationService: CallConversationService,
    private val roomSessionManager: RoomSessionManager
) {

    private val states = ConcurrentHashMap<SegmentKey, SegmentState>()
    private val locks  = ConcurrentHashMap<SegmentKey, Mutex>()

    suspend fun onUpdate(
        roomId: UUID,
        userId: UUID,
        segment: Segment,
    ) {
        val key = SegmentKey(roomId, userId, segment.id)
        val mutex = getMutex(key)
        val isChanged: Boolean
        val newState = mutex.withLock {
            val state = ensureState(roomId, userId, key)

            val next = state.copy(
                accumulatedText = buildString {
                    append(state.accumulatedText)
                    append(segment.text)
                },
                lastBuffer = segment.buffer.transcription,
                isFinal = segment.isFinal
            )
            states[key] = next

            isChanged = next.lastBuffer != state.lastBuffer || next.accumulatedText.isNotBlank()
            next
        }

        if(isChanged) broadcastSubtitle(roomId, userId, newState)
        if(newState.isFinal) updateFinalState(newState)
    }

    private suspend fun ensureState(
        roomId: UUID,
        userId: UUID,
        segmentKey: SegmentKey,
    ): SegmentState {
        return states[segmentKey] ?: run {
            val context = roomSessionManager.requireContext(roomId, userId)
            val saved = conversationService.saveConversation(
                roomId = roomId,
                participantId = context.participantId,
                senderId = userId,
                originText = "",
                originLanguage = context.language
            )
            val newState = SegmentState(key = segmentKey, conversation = saved)
            states.putIfAbsent(segmentKey, newState) ?: newState
        }
    }

    private suspend fun broadcastSubtitle(roomId: UUID, userId: UUID, state: SegmentState) {
        val payload = state.toTranslationMessage(null, null)
        val message = ServerMessage(type = MessageType.SIGNALING, payload = payload)
        roomSessionManager.broadcastMessageToRoom(roomId, message, setOf(userId))
    }

    private suspend fun updateFinalState(state: SegmentState) {
        conversationService.updateConversationText(state.conversation.id, state.originText)
    }

    private fun getMutex(segmentKey: SegmentKey): Mutex {
        return locks.computeIfAbsent(segmentKey) { Mutex() }
    }
}

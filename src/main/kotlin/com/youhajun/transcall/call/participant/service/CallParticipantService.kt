package com.youhajun.transcall.call.participant.service

import com.youhajun.transcall.call.participant.domain.CallParticipant
import com.youhajun.transcall.pagination.dto.CursorPage
import java.util.*

interface CallParticipantService {
    suspend fun checkCallParticipant(userPublicId: UUID, roomCode: UUID)
    suspend fun getCallParticipantList(userPublicId: UUID, after: String?, first: Int): CursorPage<CallParticipant>
    suspend fun joinCallParticipant(userPublicId: UUID, roomCode: UUID)
}
package com.youhajun.transcall.call.history.repository

import com.youhajun.transcall.call.history.domain.CallHistory
import java.util.*

interface CallHistoryRepositoryCustom {
    suspend fun findByRoomCodeIn(roomCodes: List<UUID>): List<CallHistory>
}
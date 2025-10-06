package com.youhajun.transcall.call.calling.service

import com.youhajun.transcall.call.calling.dto.TurnCredential
import java.util.*

interface CallingService {
    suspend fun getTurnCredential(userId: UUID): TurnCredential
}

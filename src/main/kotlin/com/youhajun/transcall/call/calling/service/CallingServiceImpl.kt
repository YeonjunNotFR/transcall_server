package com.youhajun.transcall.call.calling.service

import com.youhajun.transcall.call.calling.dto.TurnCredential
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import java.util.*

@Service
class CallingServiceImpl(
    private val transactionalOperator: TransactionalOperator,
) : CallingService {
    override suspend fun getTurnCredential(userId: UUID): TurnCredential {
        return TurnCredential(
            url = "",
            username = "",
            credential = ""
        )
    }
}
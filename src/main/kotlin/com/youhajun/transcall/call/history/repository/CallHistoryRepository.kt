package com.youhajun.transcall.call.history.repository

import com.youhajun.transcall.call.history.domain.CallHistory
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

interface CallHistoryRepository : CoroutineCrudRepository<CallHistory, UUID>, CallHistoryRepositoryCustom
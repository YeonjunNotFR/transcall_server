package com.youhajun.transcall.call.history.repository

import com.youhajun.transcall.call.history.domain.CallHistory
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CallHistoryRepository : CoroutineCrudRepository<CallHistory, UUID>, CallHistoryRepositoryCustom
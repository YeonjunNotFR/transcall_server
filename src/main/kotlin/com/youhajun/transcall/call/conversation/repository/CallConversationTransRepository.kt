package com.youhajun.transcall.call.conversation.repository

import com.youhajun.transcall.call.conversation.domain.CallConversationTrans
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

interface CallConversationTransRepository : CoroutineCrudRepository<CallConversationTrans, UUID>, CallConversationTransRepositoryCustom
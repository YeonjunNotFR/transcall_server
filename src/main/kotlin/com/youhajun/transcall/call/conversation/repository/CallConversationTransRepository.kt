package com.youhajun.transcall.call.conversation.repository

import com.youhajun.transcall.call.conversation.domain.CallConversationTrans
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CallConversationTransRepository : CoroutineCrudRepository<CallConversationTrans, UUID>, CallConversationTransRepositoryCustom
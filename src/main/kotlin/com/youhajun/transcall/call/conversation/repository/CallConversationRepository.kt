package com.youhajun.transcall.call.conversation.repository

import com.youhajun.transcall.call.conversation.domain.CallConversation
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

interface CallConversationRepository : CoroutineCrudRepository<CallConversation, UUID>, CallConversationRepositoryCustom
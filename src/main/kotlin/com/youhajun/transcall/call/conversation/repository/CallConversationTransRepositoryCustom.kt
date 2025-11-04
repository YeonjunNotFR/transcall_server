package com.youhajun.transcall.call.conversation.repository

import com.youhajun.transcall.call.conversation.domain.CallConversationTrans
import com.youhajun.transcall.user.domain.LanguageType
import java.util.*

interface CallConversationTransRepositoryCustom {

    suspend fun findByConversationIdsAndLanguage(conversationIds: List<UUID>, targetLanguage: LanguageType): List<CallConversationTrans>
}
package com.youhajun.transcall.call.conversation.exception

import com.youhajun.transcall.common.exception.ErrorType
import com.youhajun.transcall.common.exception.TransCallException

sealed class ConversationException(errorType: ErrorType) : TransCallException(errorType) {
    class ConversationSyncNeedAfter : ConversationException(ErrorType.CONVERSATION_SYNC_NEED_AFTER)
}
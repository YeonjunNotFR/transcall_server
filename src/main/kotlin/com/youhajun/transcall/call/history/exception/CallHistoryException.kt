package com.youhajun.transcall.call.history.exception

import com.youhajun.transcall.common.exception.ErrorType
import com.youhajun.transcall.common.exception.TransCallException

sealed class CallHistoryException(errorType: ErrorType): TransCallException(errorType) {
    class CallHistoryNotFoundException : CallHistoryException(ErrorType.CALL_HISTORY_NOT_FOUND)
}
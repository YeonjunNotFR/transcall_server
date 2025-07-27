package com.youhajun.transcall.call

import com.youhajun.transcall.common.exception.ErrorType
import com.youhajun.transcall.common.exception.TransCallException

sealed class CallException(errorType: ErrorType): TransCallException(errorType) {
    class ForbiddenCallNotJoin : CallException(ErrorType.FORBIDDEN_CALL_NOT_JOIN)
}
package com.youhajun.transcall.call.room.exception

import com.youhajun.transcall.common.exception.ErrorType
import com.youhajun.transcall.common.exception.TransCallException

sealed class RoomException(errorType: ErrorType) : TransCallException(errorType) {
    class RoomCodeGenerationFailed : RoomException(ErrorType.ROOM_CODE_GENERATION_FAILED)
    class RoomNotFound : RoomException(ErrorType.ROOM_NOT_FOUND)
    class RoomIsFull : RoomException(ErrorType.ROOM_IS_FULL)
}
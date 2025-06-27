package com.youhajun.transcall.common.exception

open class TransCallException(
    val errorType: ErrorType,
    override val message: String = errorType.message,
    open val data: Any? = null,
) : RuntimeException(message)
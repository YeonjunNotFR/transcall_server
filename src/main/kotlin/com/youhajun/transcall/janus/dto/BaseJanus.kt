package com.youhajun.transcall.janus.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
sealed interface BaseJanus

abstract class BaseJanusRequest : BaseJanus {
    val transaction: String = UUID.randomUUID().toString()
    abstract val janus: JanusCommand
}

interface BaseJanusResponse : BaseJanus {
    val janus: JanusResponseType
}

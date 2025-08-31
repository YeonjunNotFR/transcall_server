package com.youhajun.transcall.janus.dto

import java.util.*

abstract class BaseJanusRequest {
    val transaction: String = UUID.randomUUID().toString()
    abstract val janus: JanusCommand
}
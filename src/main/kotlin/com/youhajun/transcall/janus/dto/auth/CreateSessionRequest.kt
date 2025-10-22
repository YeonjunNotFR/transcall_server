package com.youhajun.transcall.janus.dto.auth

import com.youhajun.transcall.janus.dto.BaseJanusRequest
import com.youhajun.transcall.janus.dto.JanusCommand

class CreateSessionRequest : BaseJanusRequest() {
    override val janus: JanusCommand = JanusCommand.CREATE
}
package com.youhajun.transcall.client.janus.dto.auth

import com.youhajun.transcall.client.janus.dto.BaseJanusRequest
import com.youhajun.transcall.client.janus.dto.JanusCommand

class CreateSessionRequest : BaseJanusRequest() {
    override val janus: JanusCommand = JanusCommand.CREATE
}
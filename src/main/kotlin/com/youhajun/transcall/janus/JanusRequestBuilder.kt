package com.youhajun.transcall.janus

import com.youhajun.transcall.janus.dto.JanusRequest
import com.youhajun.transcall.janus.dto.Jsep
import java.util.*

open class JanusRequestBuilder<T> {
    protected var janus: String = "message"
    protected var sessionId: Long? = null
    protected var handleId: Long? = null
    protected var plugin: String? = null
    protected var body: T? = null
    protected var jsep: Jsep? = null
    protected var adminSecret: String? = null
    protected var candidate: Any? = null

    fun session(id: Long?) = apply { this.sessionId = id }
    fun handle(id: Long?) = apply { this.handleId = id }
    fun jsep(jsep: Jsep?) = apply { this.jsep = jsep }

    fun adminSecret(secret: String?) = apply { this.adminSecret = secret }

    fun create() = apply { this.janus = "create" }
    fun attach(plugin: String) = apply { 
        this.janus = "attach"
        this.plugin = plugin 
    }
    fun keepalive() = apply { this.janus = "keepalive" }
    fun destroy() = apply { this.janus = "destroy" }

    open fun build(): JanusRequest<T> = JanusRequest(
        janus = janus,
        transaction = UUID.randomUUID().toString(),
        sessionId = sessionId,
        handleId = handleId,
        adminSecret = adminSecret,
        plugin = plugin,
        body = body,
        jsep = jsep,
        candidate = candidate
    )
}
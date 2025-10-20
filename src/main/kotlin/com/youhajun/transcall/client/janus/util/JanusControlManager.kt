package com.youhajun.transcall.client.janus.util

import com.youhajun.transcall.client.janus.dto.plugin.JanusPlugin
import com.youhajun.transcall.client.janus.service.JanusPluginService
import com.youhajun.transcall.client.janus.service.JanusSessionService
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

@Component
class JanusControlManager(
    private val janusSessionService: JanusSessionService,
    private val janusPluginService: JanusPluginService
) {
    private val sessionRef = AtomicReference<Long?>(null)
    private val handleCache = ConcurrentHashMap<JanusPlugin, Long>()

    suspend fun getSessionId(): Long {
        return sessionRef.get() ?: createSession()
    }

    suspend fun getHandleId(plugin: JanusPlugin): Long {
        val sessionId = getSessionId()
        return handleCache[plugin] ?: attachNewHandle(sessionId, plugin)
    }

    suspend fun refreshSession() {
        val oldSession = sessionRef.get()
        janusSessionService.createManagerSession().getOrThrow().sessionId.also {
            sessionRef.set(it)
            handleCache.clear()
            oldSession?.let { old -> janusSessionService.destroyManagerSession(old) }
        }
    }

    private suspend fun createSession(): Long {
        val newSession = janusSessionService.createManagerSession().getOrThrow().sessionId
        return if(sessionRef.compareAndSet(null, newSession)) {
            handleCache.clear()
            newSession
        } else {
            janusSessionService.destroyManagerSession(newSession)
            requireNotNull(sessionRef.get())
        }
    }

    private suspend fun attachNewHandle(sid: Long, plugin: JanusPlugin): Long {
        return janusPluginService.attachManagerPlugin(sid, plugin).getOrThrow().handleId.also {
            handleCache.putIfAbsent(plugin, it)
        }
    }
}
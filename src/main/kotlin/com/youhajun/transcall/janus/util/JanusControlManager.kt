package com.youhajun.transcall.janus.util

import com.youhajun.transcall.janus.dto.plugin.JanusPlugin
import com.youhajun.transcall.janus.exception.JanusException
import com.youhajun.transcall.janus.service.JanusPluginService
import com.youhajun.transcall.janus.service.JanusSessionService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class JanusControlManager(
    private val janusSessionService: JanusSessionService,
    private val janusPluginService: JanusPluginService,
) {
    private val logger = LogManager.getLogger(javaClass)
    private val stateMutex = Mutex()
    private var sessionIdRef: Long? = null
    private val handleCache = ConcurrentHashMap<JanusPlugin, Long>()

    suspend fun getSessionId(): Long {
        val sid = stateMutex.withLock { sessionIdRef ?: createSessionUnlocked() }

        if (!isSessionAlive(sid)) {
            logger.warn("Janus session not alive. Refreshing...")
            stateMutex.withLock { refreshSessionUnlocked() }
        }

        return stateMutex.withLock { sessionIdRef!! }
    }

    suspend fun getHandleId(plugin: JanusPlugin): Long {
        val sid = stateMutex.withLock { sessionIdRef ?: createSessionUnlocked() }
        val cached = handleCache[plugin]
        return cached ?: stateMutex.withLock { attachHandleUnlocked(sid, plugin) }
    }

    suspend fun refreshSessionCheck(error: JanusException.JanusResponseException) = stateMutex.withLock {
        if (error.code in listOf(458, 459, 460)) refreshSessionUnlocked()
    }

    private suspend fun createSessionUnlocked(): Long {
        val created = janusSessionService.createManagerSession().getOrThrow().sessionId
        sessionIdRef = created
        handleCache.clear()
        logger.info("Created new Janus session: $created")
        return created
    }

    private suspend fun refreshSessionUnlocked() {
        val old = sessionIdRef
        sessionIdRef = janusSessionService.createManagerSession().getOrThrow().sessionId
        handleCache.clear()
        old?.let {
            runCatching { janusSessionService.destroyManagerSession(it) }
                .onFailure { logger.debug("Destroy old session failed: ${it.message}") }
        }
        logger.info("Refreshed Janus session: $sessionIdRef")
    }

    private suspend fun attachHandleUnlocked(sid: Long, plugin: JanusPlugin): Long {
        val hid = janusPluginService.attachManagerPlugin(sid, plugin).getOrThrow().handleId
        handleCache[plugin] = hid
        return hid
    }

    private suspend fun isSessionAlive(sid: Long): Boolean =
        janusSessionService.keepAliveManager(sid).isSuccess
}


package com.youhajun.transcall.auth.jwt

import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return runCatching {
            val token = resolveToken(exchange)
            val claims = jwtProvider.parseAccessToken(token)
            val authentication = jwtProvider.getAuthentication(token, claims)
            val context: SecurityContext = SecurityContextImpl(authentication)
            val holder = ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context))
            chain.filter(exchange).contextWrite(holder)
        }.getOrElse {
            chain.filter(exchange)
        }
    }

    private fun resolveToken(exchange: ServerWebExchange): String {
        val bearerToken = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        return bearerToken?.takeIf { it.startsWith("Bearer ") }?.substring(7).orEmpty()
    }
}
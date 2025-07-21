package com.youhajun.transcall

import com.youhajun.transcall.auth.exception.AuthException
import com.youhajun.transcall.auth.jwt.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import reactor.core.publisher.Mono


@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val jwtFilter: JwtAuthenticationFilter,
) {

    @Bean
    fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .httpBasic { it.disable() }
            .csrf { it.disable() }
            .authorizeExchange { exchange ->
                exchange.pathMatchers(
                    "/api/auth/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-resources/**",
                    "/webjars/**",
                ).permitAll().anyExchange().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint { exchange, _ ->
                    val error = AuthException.InvalidAccessTokenException()
                    return@authenticationEntryPoint Mono.error(error)
                }
            }
            .redirectToHttps {
                it.httpsRedirectWhen { exchange ->
                    val forwardedProto = exchange.request.headers.getFirst("x-forwarded-proto")
                    forwardedProto == "http"
                }
            }
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build()
    }
}
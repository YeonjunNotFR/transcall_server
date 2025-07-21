package com.youhajun.transcall.auth.jwt

import com.youhajun.transcall.auth.domain.RefreshToken
import com.youhajun.transcall.auth.domain.UserPrincipal
import com.youhajun.transcall.auth.dto.JwtTokenResponse
import com.youhajun.transcall.auth.exception.AuthException
import com.youhajun.transcall.user.domain.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

@Component
class JwtProvider(
    private val jwtConfig: JwtConfig,
) {
    companion object {
        private const val CLAIM_KEY_USER_PLAN = "plan"
    }
    private val secretKey: Key = Keys.hmacShaKeyFor(jwtConfig.secret.toByteArray())

    fun issueTokens(user: User): JwtTokenResponse {
        val now = Date()
        val accessToken = generateAccessToken(now, user)
        val refreshToken = generateRefreshToken(now, user)
        return JwtTokenResponse(accessToken, refreshToken)
    }

    fun parseAccessToken(token: String): Claims {
        val claims = try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: JwtException) {
            throw AuthException.InvalidAccessTokenException()
        }

        validateAccessToken(claims)
        return claims
    }

    fun getAuthentication(token: String, claims: Claims): Authentication {
        val userPublicId = claims.subject
        val plan = claims[CLAIM_KEY_USER_PLAN] as String
        val principal = UserPrincipal(UUID.fromString(userPublicId), plan)
        return UsernamePasswordAuthenticationToken(principal, token, listOf(SimpleGrantedAuthority(plan)))
    }

    fun validateRefreshToken(refreshToken: RefreshToken) {
        if (refreshToken.expireAt.before(Date())) {
            throw AuthException.JwtExpiredException()
        }
    }

    private fun generateAccessToken(now: Date, user: User): String {
        val expiry = Date(now.time + jwtConfig.accessTokenValidityMs)

        return Jwts.builder()
            .setSubject(user.publicId.toString())
            .claim(CLAIM_KEY_USER_PLAN, user.membershipPlan)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(secretKey)
            .compact()
    }

    private fun generateRefreshToken(now: Date, user: User): String {
        val expiry = Date(now.time + jwtConfig.refreshTokenValidityMs)

        return Jwts.builder()
            .setSubject(user.publicId.toString())
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(secretKey)
            .compact()
    }

    private fun validateAccessToken(claims: Claims) {
        if (claims.subject.isNullOrBlank()) {
            throw AuthException.InvalidAccessTokenException()
        }
        if (claims.expiration.before(Date())) {
            throw AuthException.JwtExpiredException()
        }
    }
}
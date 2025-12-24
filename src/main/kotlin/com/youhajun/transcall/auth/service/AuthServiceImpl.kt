package com.youhajun.transcall.auth.service

import com.youhajun.transcall.auth.dto.JwtTokenResponse
import com.youhajun.transcall.auth.dto.LoginRequest
import com.youhajun.transcall.auth.dto.NonceResponse
import com.youhajun.transcall.auth.exception.AuthException
import com.youhajun.transcall.auth.google.GoogleService
import com.youhajun.transcall.auth.jwt.JwtConfig
import com.youhajun.transcall.auth.jwt.JwtProvider
import com.youhajun.transcall.auth.repository.LoginNonceRepository
import com.youhajun.transcall.auth.repository.UserAuthRepository
import com.youhajun.transcall.user.domain.SocialType
import com.youhajun.transcall.user.domain.User
import com.youhajun.transcall.user.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class AuthServiceImpl(
    private val transactionalOperator: TransactionalOperator,
    private val userAuthRepository: UserAuthRepository,
    private val loginNonceRepository: LoginNonceRepository,
    private val userService: UserService,
    private val googleService: GoogleService,
    private val jwtProvider: JwtProvider,
    private val jwtConfig: JwtConfig,
) : AuthService {

    override suspend fun loginOrCreate(loginRequest: LoginRequest, remoteAddr: String?): JwtTokenResponse {
        val socialEmail = fetchSocialEmail(loginRequest)
        val user = userService.findOrCreateUser(socialEmail, loginRequest.socialType)
        return transactionalOperator.executeAndAwait {
            userAuthRepository.upsertSocialId(user.id, loginRequest.token)
            userAuthRepository.upsertLastLogin(user.id, Instant.now(), remoteAddr)
            tokenRotation(user)
        }
    }

    override suspend fun reissueToken(offerRefreshToken: String, remoteAddr: String?): JwtTokenResponse {
        val userAuth = userAuthRepository.findByRefreshToken(offerRefreshToken) ?: throw AuthException.InvalidRefreshTokenException()
        val userId = userAuth.id
        jwtProvider.validateRefreshToken(userAuth)
        val user = userService.findUserById(userId)
        return transactionalOperator.executeAndAwait {
            userAuthRepository.upsertLastLogin(user.id, Instant.now(), remoteAddr)
            tokenRotation(user)
        }
    }

    override suspend fun generateNonce(): NonceResponse {
        val loginRequestId = UUID.randomUUID().toString()
        val nonce = UUID.randomUUID().toString()
        val success = loginNonceRepository.saveNonce(loginRequestId = loginRequestId, nonce = nonce, ttlSeconds = 300)
        if(!success) throw AuthException.NonceSaveFailedException()
        return NonceResponse(nonce = nonce, loginRequestId = loginRequestId)
    }

    private suspend fun tokenRotation(user: User): JwtTokenResponse {
        return jwtProvider.issueTokens(user).also {
            val expireAt = Instant.now().plus(Duration.ofMillis(jwtConfig.refreshTokenValidityMs))
            userAuthRepository.upsertRefreshToken(user.id, it.refreshToken, expireAt)
        }
    }

    private suspend fun fetchSocialEmail(loginRequest: LoginRequest): String {
        return when (loginRequest.socialType) {
            SocialType.GOOGLE -> {
                val nonce = loginNonceRepository.getAndDeleteNonce(loginRequest.loginRequestId)
                val idToken = googleService.verifyClientToken(loginRequest.token, nonce) ?: throw AuthException.InvalidGoogleTokenException()
                val payload = idToken.payload
                if(payload.emailVerified) payload.email else throw AuthException.EmailNotVerifiedException()
            }
        }
    }
}

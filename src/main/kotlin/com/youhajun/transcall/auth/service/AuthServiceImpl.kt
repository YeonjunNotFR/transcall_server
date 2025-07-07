package com.youhajun.transcall.auth.service

import com.youhajun.transcall.auth.domain.RefreshToken
import com.youhajun.transcall.auth.dto.JwtTokenResponse
import com.youhajun.transcall.auth.dto.LoginRequest
import com.youhajun.transcall.auth.exception.AuthException
import com.youhajun.transcall.auth.google.GoogleService
import com.youhajun.transcall.auth.jwt.JwtConfig
import com.youhajun.transcall.auth.jwt.JwtProvider
import com.youhajun.transcall.auth.repository.RefreshTokenRepository
import com.youhajun.transcall.user.domain.SocialType
import com.youhajun.transcall.user.domain.User
import com.youhajun.transcall.user.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.*

@Service
class AuthServiceImpl(
    private val transactionalOperator: TransactionalOperator,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userService: UserService,
    private val googleService: GoogleService,
    private val jwtProvider: JwtProvider,
    private val jwtConfig: JwtConfig,
) : AuthService {

    override suspend fun loginOrCreate(loginRequest: LoginRequest): JwtTokenResponse {
        val socialEmail = fetchSocialEmail(loginRequest)
        return transactionalOperator.executeAndAwait {
            val user = userService.findOrCreateUser(socialEmail, loginRequest.socialType)
            tokenRotation(user)
        }
    }

    override suspend fun reissueToken(offerRefreshToken: String): JwtTokenResponse {
        val refreshToken = refreshTokenRepository.findByToken(offerRefreshToken) ?: throw AuthException.InvalidRefreshTokenException()
        jwtProvider.validateRefreshToken(refreshToken)
        return transactionalOperator.executeAndAwait {
            val user = userService.findUserByPublicId(refreshToken.userPublicId)
            tokenRotation(user)
        }
    }

    private suspend fun tokenRotation(user: User): JwtTokenResponse {
        val userPublicId = user.publicId.toString()
        return jwtProvider.issueTokens(user).also {
            refreshTokenRepository.deleteByUserPublicId(userPublicId)
            saveRefreshToken(it.refreshToken, userPublicId)
        }
    }

    private suspend fun saveRefreshToken(refreshToken: String, userPublicId: String) {
        val expireAt = Date(Date().time + jwtConfig.refreshTokenValidityMs)
        val newRefreshToken = RefreshToken(token = refreshToken, userPublicId = userPublicId, expireAt = expireAt)
        refreshTokenRepository.save(newRefreshToken)
    }

    private suspend fun fetchSocialEmail(loginRequest: LoginRequest): String {
        return when (loginRequest.socialType) {
            SocialType.GOOGLE -> {
                val token = googleService.fetchToken(loginRequest.authorizationCode)
                val userInfo = googleService.fetchUserInfo(token.access_token)
                userInfo.email
            }
        }
    }
}
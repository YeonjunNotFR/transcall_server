package com.youhajun.transcall.user.service

import com.youhajun.transcall.user.domain.SocialType
import com.youhajun.transcall.user.domain.User
import com.youhajun.transcall.user.domain.UserQuota
import com.youhajun.transcall.user.domain.UserSettings
import com.youhajun.transcall.user.dto.MyInfoResponse
import com.youhajun.transcall.user.exception.UserException
import com.youhajun.transcall.user.repository.UserQuotaRepository
import com.youhajun.transcall.user.repository.UserRepository
import com.youhajun.transcall.user.repository.UserSettingsRepository
import com.youhajun.transcall.user.vo.UserTermsUpdateRequestVo
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userQuotaRepository: UserQuotaRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val transactionalOperator: TransactionalOperator
) : UserService {

    companion object {
        private const val INITIAL_QUOTA_SECONDS = 3600L
    }

    override suspend fun findOrCreateUser(email: String, socialType: SocialType): User {
        return transactionalOperator.executeAndAwait {
            userRepository.findUserByEmail(email) ?: createUser(email, socialType)
        }
    }

    override suspend fun findUserById(id: UUID): User {
        return userRepository.findById(id) ?: throw UserException.UserNotFoundException()
    }

    override suspend fun getMyInfo(userId: UUID): MyInfoResponse {
        return findUserById(userId).toMyInfoResponse()
    }

    override suspend fun isEmailExist(email: String): Boolean {
        return userRepository.existsUserByEmail(email)
    }

    override suspend fun updateUserTerms(userId: UUID, request: UserTermsUpdateRequestVo): UserSettings {
        return transactionalOperator.executeAndAwait {
            val current = userSettingsRepository.findById(userId) ?: UserSettings(uuid = userId)
            val resolvedPrivacyVersion = request.isPrivacyAgreed?.let { Instant.now().toString() } ?: current.privacyVersion
            val resolvedMarketingAgreedAt = request.isMarketingAgreed?.let { Instant.now() } ?: current.marketingAgreedAt

            val updated = current.copy(
                isPushEnabled = request.isPushEnabled ?: current.isPushEnabled,
                isMarketingAgreed = request.isMarketingAgreed ?: current.isMarketingAgreed,
                isPrivacyAgreed = request.isPrivacyAgreed ?: current.isPrivacyAgreed,
                isTermsAgreed = request.isTermsAgreed ?: current.isTermsAgreed,
                privacyVersion = resolvedPrivacyVersion,
                marketingAgreedAt = resolvedMarketingAgreedAt
            )
            userSettingsRepository.save(updated)
        }
    }

    private suspend fun createUser(email: String, socialType: SocialType): User {
        val newUser = User(email = email, socialType = socialType, nickname = generateRandomNickname())
        return userRepository.save(newUser).also {
            createQuota(it.id)
            createSettings(it.id)
        }
    }

    private suspend fun createQuota(userId: UUID) {
        val resetAt: Instant = Instant.now().plus(1, ChronoUnit.DAYS)
        val quota = UserQuota(uuid = userId, remainingSeconds = INITIAL_QUOTA_SECONDS, resetAt = resetAt)
        userQuotaRepository.save(quota)
    }

    private suspend fun createSettings(userId: UUID) {
        userSettingsRepository.save(UserSettings(uuid = userId))
    }

    private fun generateRandomNickname(): String {
        val adjectives = listOf("Brave", "Swift", "Silent", "Mighty", "Happy")
        val animals = listOf("Tiger", "Eagle", "Fox", "Shark", "Panda")
        return "${adjectives.random()}${animals.random()}${(100..999).random()}"
    }
}

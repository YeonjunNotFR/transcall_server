package com.youhajun.transcall.user.service

import com.youhajun.transcall.user.domain.SocialType
import com.youhajun.transcall.user.domain.User
import com.youhajun.transcall.user.domain.UserQuota
import com.youhajun.transcall.user.dto.MyInfoResponse
import com.youhajun.transcall.user.exception.UserException
import com.youhajun.transcall.user.repository.UserQuotaRepository
import com.youhajun.transcall.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userQuotaRepository: UserQuotaRepository,
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
        val userQuota = userQuotaRepository.findByUserId(userId)
        return findUserById(userId).toMyInfoResponse(userQuota.toRemainTimeResponse())
    }

    override suspend fun isEmailExist(email: String): Boolean {
        return userRepository.existsUserByEmail(email)
    }

    private suspend fun createUser(email: String, socialType: SocialType): User {
        val newUser = User(email = email, socialType = socialType, nickname = generateRandomNickname())
        return userRepository.save(newUser).also {
            createQuota(it.id)
        }
    }

    private suspend fun createQuota(userId: UUID) {
        val resetAt: LocalDateTime = LocalDate.now().plusDays(1).atStartOfDay()
        val quota = UserQuota(userId = userId, remainingSeconds = INITIAL_QUOTA_SECONDS, resetAt = resetAt)
        userQuotaRepository.save(quota)
    }

    private fun generateRandomNickname(): String {
        val adjectives = listOf("Brave", "Swift", "Silent", "Mighty", "Happy")
        val animals = listOf("Tiger", "Eagle", "Fox", "Shark", "Panda")
        return "${adjectives.random()}${animals.random()}${(100..999).random()}"
    }
}

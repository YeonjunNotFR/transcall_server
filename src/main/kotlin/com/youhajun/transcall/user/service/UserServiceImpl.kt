package com.youhajun.transcall.user.service

import com.youhajun.transcall.user.domain.SocialType
import com.youhajun.transcall.user.domain.User
import com.youhajun.transcall.user.exception.UserException
import com.youhajun.transcall.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
import java.util.*

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val transactionalOperator: TransactionalOperator
) : UserService {

    override suspend fun findOrCreateUser(email: String, socialType: SocialType): User {
        return transactionalOperator.executeAndAwait {
            userRepository.findByEmail(email) ?: createAndSaveUser(email, socialType)
        }
    }

    override suspend fun findUserByPublicId(publicId: String): User {
        return userRepository.findByPublicId(UUID.fromString(publicId))
            ?: throw UserException.UserNotFoundException()
    }

    override suspend fun isEmailExist(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }

    private suspend fun createAndSaveUser(email: String, socialType: SocialType): User {
        val newUser = User(email = email, socialType = socialType, nickname = generateRandomNickname())
        return userRepository.save(newUser)
    }

    private fun generateRandomNickname(): String {
        val adjectives = listOf("Brave", "Swift", "Silent", "Mighty", "Happy")
        val animals = listOf("Tiger", "Eagle", "Fox", "Shark", "Panda")
        return "${adjectives.random()}${animals.random()}${(100..999).random()}"
    }
}

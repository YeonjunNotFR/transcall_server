package com.youhajun.transcall.user.repository

import com.youhajun.transcall.user.domain.User
import org.springframework.data.r2dbc.core.*
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import java.util.*

class UserRepositoryCustomImpl(
    template: R2dbcEntityTemplate
) : UserRepositoryCustom {

    private val select = template.select<User>()

    override suspend fun existsUserByEmail(email: String): Boolean {
        return select
            .matching(Query.query(Criteria.where("email").`is`(email)))
            .awaitExists()
    }

    override suspend fun findUserByEmail(email: String): User? {
        return select
            .matching(Query.query(Criteria.where("email").`is`(email)))
            .awaitOneOrNull()
    }

    override suspend fun findUserByPublicId(publicId: UUID): User? {
        return select
            .matching(Query.query(Criteria.where("public_id").`is`(publicId)))
            .awaitOneOrNull()
    }
}
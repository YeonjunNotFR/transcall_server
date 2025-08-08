package com.youhajun.transcall.user.repository

import com.youhajun.transcall.user.domain.User
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitExists
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.r2dbc.core.select
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query

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
}
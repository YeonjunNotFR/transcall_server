package com.youhajun.transcall.user.repository

import com.youhajun.transcall.user.domain.UserQuota
import com.youhajun.transcall.user.exception.UserException
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.r2dbc.core.select
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import java.util.*

class UserQuotaRepositoryCustomImpl(
    template: R2dbcEntityTemplate
) : UserQuotaRepositoryCustom {

    private val select = template.select<UserQuota>()

    override suspend fun findByUserId(userId: UUID): UserQuota {
        val criteria = Criteria.where("user_id").`is`(userId)
        return select
            .matching(Query.query(criteria))
            .awaitOneOrNull() ?: throw UserException.UserQuotaNotFoundException()
    }
}
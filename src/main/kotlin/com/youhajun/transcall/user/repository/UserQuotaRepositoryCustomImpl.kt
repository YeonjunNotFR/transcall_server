package com.youhajun.transcall.user.repository

import com.youhajun.transcall.user.domain.UserQuota
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

    override suspend fun findByUserPublicId(userPublicId: UUID): UserQuota? {
        return select
            .matching(Query.query(Criteria.where("user_public_id").`is`(userPublicId)))
            .awaitOneOrNull()
    }
}
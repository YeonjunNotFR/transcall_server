package com.youhajun.transcall.auth.repository

import com.youhajun.transcall.auth.domain.RefreshToken
import org.springframework.data.r2dbc.core.*
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import java.util.*

class UserRepositoryCustomImpl(
    template: R2dbcEntityTemplate
) : RefreshTokenRepositoryCustom {

    private val select = template.select<RefreshToken>()
    private val delete = template.delete<RefreshToken>()

    override suspend fun findByToken(token: String): RefreshToken? {
        return select
            .matching(Query.query(Criteria.where("token").`is`(token)))
            .awaitFirstOrNull()
    }

    override suspend fun deleteByUserPublicId(publicId: UUID) {
        delete
            .matching(Query.query(Criteria.where("publicId").`is`(publicId)))
            .allAndAwait()
    }
}
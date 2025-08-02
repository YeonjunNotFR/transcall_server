package com.youhajun.transcall.auth.repository

import com.youhajun.transcall.auth.domain.RefreshToken
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitFirstOrNull
import org.springframework.data.r2dbc.core.delete
import org.springframework.data.r2dbc.core.select
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import java.util.*

class RefreshTokenRepositoryCustomImpl(
    template: R2dbcEntityTemplate
) : RefreshTokenRepositoryCustom {

    private val select = template.select<RefreshToken>()
    private val delete = template.delete<RefreshToken>()

    override suspend fun findByToken(token: String): RefreshToken? {
        return select
            .matching(Query.query(Criteria.where("token").`is`(token)))
            .awaitFirstOrNull()
    }

    override suspend fun deleteByUserPublicId(userPublicId: UUID) {
        delete
            .matching(Query.query(Criteria.where("user_public_id").`is`(userPublicId)))
            .all()
            .awaitSingleOrNull()
    }
}
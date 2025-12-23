package com.youhajun.transcall.auth.repository

import com.youhajun.transcall.auth.domain.UserAuth
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitFirstOrNull
import org.springframework.data.r2dbc.core.select
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.bind
import java.time.Instant
import java.util.UUID

@Repository
class UserAuthRepositoryCustomImpl(
    template: R2dbcEntityTemplate,
    private val databaseClient: DatabaseClient
) : UserAuthRepositoryCustom {

    private val select = template.select<UserAuth>()

    override suspend fun findByRefreshToken(refreshToken: String): UserAuth? {
        return select
            .matching(Query.query(Criteria.where("refresh_token").`is`(refreshToken)))
            .awaitFirstOrNull()
    }

    override suspend fun upsertRefreshToken(userId: UUID, refreshToken: String, expireAt: Instant) {
        val sql = """
            INSERT INTO user_auth (user_id, refresh_token, token_expire_at, updated_at)
            VALUES (:userId, :refreshToken, :expireAt, NOW())
            ON CONFLICT (user_id) DO UPDATE
            SET refresh_token = EXCLUDED.refresh_token,
                token_expire_at = EXCLUDED.token_expire_at,
                updated_at = NOW()
        """.trimIndent()

        databaseClient.sql(sql)
            .bind("userId", userId)
            .bind("refreshToken", refreshToken)
            .bind("expireAt", expireAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    override suspend fun upsertLastLogin(userId: UUID, lastLoginAt: Instant, lastLoginIp: String?) {
        val sql = """
            INSERT INTO user_auth (user_id, last_login_at, last_login_ip, updated_at)
            VALUES (:userId, :lastLoginAt, :lastLoginIp, NOW())
            ON CONFLICT (user_id) DO UPDATE
            SET last_login_at = EXCLUDED.last_login_at,
                last_login_ip = EXCLUDED.last_login_ip,
                updated_at = NOW()
        """.trimIndent()

        databaseClient.sql(sql)
            .bind("userId", userId)
            .bind("lastLoginAt", lastLoginAt)
            .bind("lastLoginIp", lastLoginIp)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    override suspend fun upsertSocialId(userId: UUID, socialId: String) {
        val sql = """
            INSERT INTO user_auth (user_id, social_id, updated_at)
            VALUES (:userId, :socialId, NOW())
            ON CONFLICT (user_id) DO UPDATE
            SET social_id = EXCLUDED.social_id,
                updated_at = NOW()
        """.trimIndent()

        databaseClient.sql(sql)
            .bind("userId", userId)
            .bind("socialId", socialId)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}

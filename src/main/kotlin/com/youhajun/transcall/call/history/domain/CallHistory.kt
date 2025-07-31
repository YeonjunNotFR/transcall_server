package com.youhajun.transcall.call.history.domain

import com.youhajun.transcall.common.domain.BaseEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("call_history")
data class CallHistory(
    @Id
    @Column("id")
    val id: UUID? = null,
    @Column("roomCode")
    val roomCode: UUID,
    @Column("started_at")
    val startedAt: LocalDateTime,
    @Column("ended_at")
    val endedAt: LocalDateTime,
    @Column("duration_seconds")
    val durationSeconds: Int,
): BaseEntity()
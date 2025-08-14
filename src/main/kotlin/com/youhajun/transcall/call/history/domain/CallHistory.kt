package com.youhajun.transcall.call.history.domain

import org.springframework.data.relational.core.mapping.Table
import com.fasterxml.uuid.Generators
import com.youhajun.transcall.common.domain.BaseUUIDEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import java.time.LocalDateTime
import java.util.*

@Table("call_history")
data class CallHistory(
    @Id
    @Column("id")
    override val id: UUID = Generators.timeBasedEpochRandomGenerator().generate(),
    @Column("room_id")
    val roomId: UUID,
    @Column("user_id")
    val userId: UUID?,
    @Column("title")
    val title: String,
    @Column("summary")
    val summary: String?,
    @Column("memo")
    val memo: String?,
    @Column("liked")
    val liked: Boolean = false,
    @Column("deleted")
    val deleted: Boolean = false,
    @Column("joined_at")
    val joinedAt: LocalDateTime,
    @Column("left_at")
    val leftAt: LocalDateTime?,
    @Column("left_reason")
    val leftReason: String?,
) : BaseUUIDEntity()

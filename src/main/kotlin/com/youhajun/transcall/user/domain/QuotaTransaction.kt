package com.youhajun.transcall.user.domain

import com.fasterxml.uuid.Generators
import com.youhajun.transcall.common.domain.BaseUUIDEntity
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table("quota_transactions")
data class QuotaTransaction(
    @Id
    @Column("id")
    override val uuid: UUID = Generators.timeBasedEpochRandomGenerator().generate(),
    @Column("user_id")
    val userId: UUID,
    @Column("amount")
    val amount: Long,
    @Column("type")
    val type: String,
    @Column("reference_id")
    val referenceId: String? = null,
) : BaseUUIDEntity()

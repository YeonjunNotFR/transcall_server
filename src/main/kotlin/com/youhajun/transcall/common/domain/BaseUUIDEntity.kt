package com.youhajun.transcall.common.domain

import org.springframework.data.domain.Persistable
import java.util.*

abstract class BaseUUIDEntity : Persistable<UUID>, BaseEntity() {

    @Transient
    private var isNewEntity: Boolean = true

    abstract val id: UUID

    override fun getId(): UUID = id

    override fun isNew(): Boolean = isNewEntity

    fun markAsNotNew() {
        isNewEntity = false
    }
}
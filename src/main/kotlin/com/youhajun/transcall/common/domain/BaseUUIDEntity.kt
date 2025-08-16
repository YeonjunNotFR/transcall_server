package com.youhajun.transcall.common.domain

import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import java.util.*

abstract class BaseUUIDEntity : Persistable<UUID>, BaseEntity() {

    @Transient
    private var isNewEntity: Boolean = true

    abstract val uuid: UUID

    override fun getId(): UUID = uuid

    override fun isNew(): Boolean = isNewEntity

    fun markAsNotNew() {
        isNewEntity = false
    }
}
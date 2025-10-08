package com.youhajun.transcall.common.domain

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant

abstract class BaseEntity {
    @CreatedDate
    var createdAt: Instant = Instant.now()

    @LastModifiedDate
    var updatedAt: Instant = Instant.now()
}
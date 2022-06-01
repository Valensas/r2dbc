package com.valensas.common.auditing.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import java.time.Instant

open class BaseEntity<T>(
    @Id
    open val id: T? = null,
    @CreatedDate
    open val createdDate: Instant? = null
)

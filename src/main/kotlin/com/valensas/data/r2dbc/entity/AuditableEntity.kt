package com.valensas.data.r2dbc.entity

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant

open class AuditableEntity<T>(
    @Id
    open val id: T? = null,
    @CreatedDate
    open val createdDate: Instant? = null,
    @CreatedBy
    open val createdBy: String? = null,
    @LastModifiedDate
    open val updatedDate: Instant? = null,
    @LastModifiedBy
    open val updatedBy: String? = null
)

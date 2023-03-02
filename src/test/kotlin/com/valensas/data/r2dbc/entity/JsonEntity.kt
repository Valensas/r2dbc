package com.valensas.data.r2dbc.entity

import com.valensas.data.r2dbc.annotation.PgJson
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("json_entity")
data class JsonEntity(
    override val id: Long? = null,
    override val createdBy: String? = null,
    override val createdDate: Instant? = null,
    override val updatedBy: String? = null,
    override val updatedDate: Instant? = null,
    @PgJson
    val data: Map<String, String>
) : AuditableEntity<Long>(id, createdDate, createdBy, updatedDate, updatedBy)

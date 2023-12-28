package com.valensas.data.r2dbc.entity

import com.valensas.data.r2dbc.annotation.PgEnum
import com.valensas.data.r2dbc.annotation.PgJson
import com.valensas.data.r2dbc.model.Audit
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("enum_entity")
data class EnumEntity(
    override val id: Long? = null,
    @PgJson
    override val createdBy: Audit? = null,
    override val createdDate: Instant? = null,
    @PgJson
    override val updatedBy: Audit? = null,
    override val updatedDate: Instant? = null,
    @PgEnum("enum_entity_type")
    val type: Type,
) : AuditableEntity<Long, Audit>(id, createdDate, createdBy, updatedDate, updatedBy) {
    enum class Type {
        Type1,
        Type2,
    }
}

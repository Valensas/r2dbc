package com.valensas.data.r2dbc.entity

import com.valensas.data.r2dbc.annotation.PgEnum
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("enum_entity")
data class EnumEntity(
    override val id: Long? = null,
    override val createdBy: String? = null,
    override val createdDate: Instant? = null,
    override val updatedBy: String? = null,
    override val updatedDate: Instant? = null,
    @PgEnum("enum_entity_type")
    val type: Type
) : AuditableEntity<Long>(id, createdDate, createdBy, updatedDate, updatedBy) {
    enum class Type {
        Type1,
        Type2
    }
}

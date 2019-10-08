package com.valensas.common.auditing.entity

import com.valensas.common.auditing.converter.AuditStrConverter
import com.valensas.common.auditing.model.AuditStr
import com.valensas.common.auditing.model.ID
import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "active = true")
abstract class AuditableEntity(

    /**
     * @SQLDelete(sql = "UPDATE child SET active = 0 WHERE id = ?")
     * class Child (
     *   id: ID? = null,
     *   ...
     * ) : AuditableEntity(id = id)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(AccessType.PROPERTY)
    override var id: ID?,

    @Column(nullable = false, updatable = false)
    override var active: Boolean? = true,

    @CreatedBy
    @Column(nullable = true, updatable = false)
    @Convert(converter = AuditStrConverter::class)
    override var createdBy: AuditStr? = null,

    @CreatedDate
    @Column(nullable = true, updatable = false)
    override var createdDate: Instant? = null,

    @LastModifiedBy
    @Convert(converter = AuditStrConverter::class)
    @Column(nullable = true, insertable = false)
    override var lastModifiedBy: AuditStr? = null,

    @LastModifiedDate
    @Column(nullable = true, insertable = false)
    override var lastModifiedDate: Instant? = null
) : Auditable<AuditStr>, Base<ID?>
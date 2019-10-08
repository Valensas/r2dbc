package com.valensas.common.auditing.entity

import com.valensas.common.auditing.model.ID
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Column
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(AccessType.PROPERTY)
    override var id: ID?,

    @CreatedDate
    @Column(nullable = true, updatable = false)
    override var createdDate: Instant? = null
) : Base<ID?>
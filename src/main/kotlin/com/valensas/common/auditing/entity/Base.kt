package com.valensas.common.auditing.entity

import java.time.Instant

interface Base<ID> {
    /**
     * Generic id annotations
     *
     * @Id
     * @GeneratedValue(strategy = GenerationType.IDENTITY)
     * @Access(AccessType.PROPERTY)
     * var id: ID? = null
     **/
    var id: ID
    var createdDate: Instant?
}
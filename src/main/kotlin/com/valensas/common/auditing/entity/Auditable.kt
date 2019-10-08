package com.valensas.common.auditing.entity

import java.time.Instant

interface Auditable<T> {
    var active: Boolean?

    var createdBy: T?
    var createdDate: Instant?

    var lastModifiedBy: T?
    var lastModifiedDate: Instant?
}
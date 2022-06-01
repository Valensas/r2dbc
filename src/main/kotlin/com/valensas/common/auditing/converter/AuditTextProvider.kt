package com.valensas.common.auditing.converter

import reactor.core.publisher.Mono

interface AuditTextProvider {
    fun getAuditString(): Mono<String>
}

package com.valensas.data.r2dbc.converter

import reactor.core.publisher.Mono

interface AuditTextProvider {
    fun getAuditString(): Mono<String>
}

package com.valensas.data.r2dbc.config

import com.valensas.data.r2dbc.converter.AuditTextProvider
import kotlinx.coroutines.reactor.mono
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.ReactiveAuditorAware
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Configuration
@EnableR2dbcAuditing
class AuditingAutoConfiguration {
    @Bean
    @ConditionalOnBean(AuditTextProvider::class)
    fun auditorAware(auditTextProvider: AuditTextProvider): ReactiveAuditorAware<String>? {
        return ReactiveAuditorAware<String> {
            auditTextProvider.getAuditString()
        }
    }

    @Component
    @ConditionalOnMissingBean(AuditTextProvider::class)
    class AuditProvider : AuditTextProvider {
        override fun getAuditString(): Mono<String> {
            val usernameSubscriber =
                ReactiveSecurityContextHolder.getContext()
                    .map { it.authentication }
                    .mapNotNull { (it.principal as? OAuth2AuthenticatedPrincipal)?.name }
                    .switchIfEmpty { mono { "anonymous" } }

            val ipSubscriber =
                Mono.deferContextual { ctx ->
                    mono { ctx.getOrDefault<String>("ClientIp", null) }
                }.switchIfEmpty { mono { "no-ip" } }

            return Mono.zip(usernameSubscriber, ipSubscriber).map {
                "${it.t1} - ${it.t2}"
            }
        }
    }
}

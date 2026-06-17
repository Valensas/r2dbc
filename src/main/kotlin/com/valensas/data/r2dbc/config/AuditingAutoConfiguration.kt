package com.valensas.data.r2dbc.config

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.reactor.mono
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.auditing.ReactiveAuditingHandler
import org.springframework.data.domain.ReactiveAuditorAware
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Configuration
@EnableR2dbcAuditing
class AuditingAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ReactiveAuditorAware::class)
    fun auditProvider(): ReactiveAuditorAware<String> = AuditProvider()

    class AuditProvider : ReactiveAuditorAware<String> {
        override fun getCurrentAuditor(): Mono<String> {
            val usernameSubscriber =
                ReactiveSecurityContextHolder
                    .getContext()
                    .mapNotNull { (it.authentication.principal as? OAuth2AuthenticatedPrincipal)?.name }
                    .switchIfEmpty { mono { "anonymous" } }

            val ipSubscriber =
                Mono
                    .deferContextual { ctx ->
                        mono { ctx.getOrDefault<String>("ClientIp", null) }
                    }.switchIfEmpty { mono { "no-ip" } }

            return Mono.zip(usernameSubscriber, ipSubscriber).map {
                "${it.t1} - ${it.t2}"
            }
        }
    }
}

/**
 * On native images, the ReactiveAuditingHandler's setAuditorAware method is never called,
 * causing createdBy and updatedBy columns to left empty. This is a workaround that sets the
 * ReactiveAuditorAware manually.
 */
@Configuration
@ConditionalOnBean(ReactiveAuditorAware::class, ReactiveAuditingHandler::class)
class AuditingPatchAutoConfiguration(
    private val auditorAware: ReactiveAuditorAware<*>,
    private val auditHandler: ReactiveAuditingHandler
) {
    @PostConstruct
    fun patch() {
        auditHandler.setAuditorAware(auditorAware)
    }
}

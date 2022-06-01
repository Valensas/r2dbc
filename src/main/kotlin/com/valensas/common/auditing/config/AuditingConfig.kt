package com.valensas.common.auditing.config
import com.valensas.common.auditing.converter.AuditTextProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.ReactiveAuditorAware
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing

@Configuration
@EnableR2dbcAuditing
class AuditingConfig {
    @Bean
    fun auditorAware(
        auditTextProvider: AuditTextProvider
    ): ReactiveAuditorAware<String>? {
        return ReactiveAuditorAware<String> {
            auditTextProvider.getAuditString()
        }
    }
}

package com.valensas.data.r2dbc.config

import com.valensas.data.r2dbc.model.Audit
import kotlinx.coroutines.reactor.mono
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.ReactiveAuditorAware
import java.net.InetAddress

@Configuration
class TestAuditConfiguration {
    @Bean
    fun auditProvider(): ReactiveAuditorAware<Audit> = ReactiveAuditorAware {
        mono {
            Audit("username", InetAddress.getLocalHost())
        }
    }
}

package com.valensas.data.r2dbc.config

import io.r2dbc.pool.ConnectionPool
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Configuration
@EnableScheduling
@AutoConfigureAfter(DatabaseAutoConfiguration::class)
@ConditionalOnProperty("spring.r2dbc.pool.enabled", havingValue = "true", matchIfMissing = true)
class ConnectionPoolConfig(
    private val abstractR2dbcConfiguration: AbstractR2dbcConfiguration,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        logger.info("Connection pool auto warmup is enabled.")
    }

    @Scheduled(
        fixedDelayString = "\${spring.r2dbc.pool.warmup.fixedDelay:300000}",
        initialDelayString = "\${spring.r2dbc.pool.warmup.initialDelay:600000}",
    )
    fun warmUp() {
        val connectionPool = abstractR2dbcConfiguration.connectionFactory() as? ConnectionPool ?: return
        logger.debug("Database connection pool warm up is triggered.")
        connectionPool.warmup().subscribe {
            logger.info("Database connection pool warm up is completed.")
        }
    }
}

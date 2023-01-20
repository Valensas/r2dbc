package com.valensas.data.r2dbc.config

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.spi.ConnectionFactory
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Configuration
@EnableScheduling
@AutoConfigureAfter(DatabaseAutoConfiguration::class)
@ConditionalOnBean(ConnectionFactory::class)
@ConditionalOnClass(ConnectionPool::class)
class ConnectionPoolConfig(
    private val abstractR2dbcConfiguration: AbstractR2dbcConfiguration
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 300000, initialDelay = 600000)
    fun warmUp() {
        val connectionPool = abstractR2dbcConfiguration.connectionFactory() as? ConnectionPool ?: return
        logger.info("Database connection warm up is triggered.")
        connectionPool.warmup().subscribe {
            logger.info("Database connection warm up is completed.")
        }
    }
}

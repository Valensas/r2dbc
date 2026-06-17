package com.valensas.data.r2dbc.config

import io.r2dbc.pool.ConnectionPool
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import reactor.pool.SimpleDequePool

@Configuration
@EnableScheduling
@AutoConfigureAfter(DatabaseAutoConfiguration::class)
@ConditionalOnProperty("spring.r2dbc.pool.enabled", havingValue = "true", matchIfMissing = true)
class ConnectionPoolConfig(
    private val abstractR2dbcConfiguration: AbstractR2dbcConfiguration
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        logger.info("Connection pool auto warmup is enabled.")
    }

    @Scheduled(
        fixedDelayString = "\${spring.r2dbc.pool.warmup.fixedDelay:60000}",
        initialDelayString = "\${spring.r2dbc.pool.warmup.initialDelay:600000}"
    )
    fun warmUp() {
        val connectionPool = abstractR2dbcConfiguration.connectionFactory() as? ConnectionPool ?: return
        logger.debug("Database connection pool warm up is triggered.")

        val connectionPoolField = connectionPool::class.java.getDeclaredField("connectionPool")
        connectionPoolField.isAccessible = true
        val pool = connectionPoolField.get(connectionPool) as SimpleDequePool<*>
        val allocationStrategy = pool.config().allocationStrategy()
        val currentConnections = allocationStrategy.permitGranted()
        val minIdleCount = allocationStrategy.permitMinimum()
        if (currentConnections < minIdleCount) {
            logger.info(
                "Current connection count: {} is less than {}. Warmup required.",
                currentConnections,
                minIdleCount
            )
            connectionPool.warmup().subscribe {
                logger.info("Database connection pool warm up is completed.")
            }
        } else {
            logger.debug(
                "Current connection count: {} is qe than {}. Warmup not required.",
                currentConnections,
                minIdleCount
            )
        }
    }
}

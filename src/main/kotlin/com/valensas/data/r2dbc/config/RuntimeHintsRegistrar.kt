package com.valensas.data.r2dbc.config

import io.r2dbc.pool.ConnectionPool
import org.springframework.aot.hint.ExecutableMode
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import java.time.OffsetDateTime
import kotlin.reflect.jvm.javaMethod

class RuntimeHintsRegistrar : RuntimeHintsRegistrar {
    override fun registerHints(
        hints: RuntimeHints,
        classLoader: ClassLoader?
    ) {
        hints
            .reflection()
            // Required for Instant column types
            .registerType(OffsetDateTime::class.java)
            .registerMethod(OffsetDateTime::toInstant.javaMethod!!, ExecutableMode.INVOKE)
            // Required for connection pool warm-up
            .registerType(ConnectionPool::class.java)
            .registerField(ConnectionPool::class.java.getDeclaredField("connectionPool"))
    }
}

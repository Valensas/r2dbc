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
        classLoader: ClassLoader?,
    ) {
        val clazz = classLoader?.loadClass("io.r2dbc.pool.ConnectionPool") ?: return
        val connectionPoolField = clazz.getDeclaredField("connectionPool")

        hints.reflection()
            .registerType(OffsetDateTime::class.java)
            .registerType(ConnectionPool::class.java)
            .registerMethod(OffsetDateTime::toInstant.javaMethod!!, ExecutableMode.INVOKE)
            .registerField(connectionPoolField)
    }
}

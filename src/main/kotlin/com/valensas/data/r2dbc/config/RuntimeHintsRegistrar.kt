package com.valensas.data.r2dbc.config

import org.springframework.aot.hint.BindingReflectionHintsRegistrar
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar

class RuntimeHintsRegistrar : RuntimeHintsRegistrar {
    override fun registerHints(
        hints: RuntimeHints,
        classLoader: ClassLoader?,
    ) {
        val classNames =
            listOf(
                "java.time.OffsetDateTime",
                "io.r2dbc.pool.ConnectionPool",
            )
        classNames.forEach { className ->
            val clazz = classLoader?.loadClass(className) ?: return@forEach
            BindingReflectionHintsRegistrar().registerReflectionHints(hints.reflection(), clazz)
        }
    }
}

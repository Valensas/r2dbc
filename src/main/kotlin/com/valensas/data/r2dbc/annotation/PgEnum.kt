package com.valensas.data.r2dbc.annotation

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class PgEnum(
    val name: String
)

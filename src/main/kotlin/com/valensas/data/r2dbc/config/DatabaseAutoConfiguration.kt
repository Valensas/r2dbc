package com.valensas.data.r2dbc.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.valensas.data.r2dbc.annotation.PgEnum
import com.valensas.data.r2dbc.annotation.PgJson
import com.valensas.data.r2dbc.converter.CustomPostgresEnumConverter
import com.valensas.data.r2dbc.converter.CustomPostgresJsonReadingConverter
import com.valensas.data.r2dbc.converter.CustomPostgresJsonWritingConverter
import com.valensas.data.r2dbc.converter.DurationToIntervalConverter
import com.valensas.data.r2dbc.converter.IntervalToDurationConverter
import com.valensas.data.r2dbc.converter.JsonToMapConverter
import com.valensas.data.r2dbc.converter.MapToJsonConverter
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.client.SSLMode
import io.r2dbc.postgresql.codec.EnumCodec
import io.r2dbc.postgresql.extension.CodecRegistrar
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.data.util.CustomCollections
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType

@Configuration
@EnableR2dbcRepositories
@EnableConfigurationProperties(R2dbcProperties::class)
@RegisterReflectionForBinding(
    // Type required enum columns
    java.lang.Enum.EnumDesc::class,
)
class DatabaseAutoConfiguration(
    private val prop: R2dbcProperties,
    private val context: ApplicationContext,
    private val objectMapper: ObjectMapper,
    private val converters: List<Converter<*, *>>,
) : AbstractR2dbcConfiguration() {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean
    override fun connectionFactory(): ConnectionFactory {
        val options = ConnectionFactoryOptions.parse(prop.url)
        val host = options.getValue(Option.valueOf<String>("host")) as String
        val port = options.getValue(Option.valueOf<String>("port")) as Int
        val database = options.getValue(Option.valueOf<String>("database")) as String
        val preparedStatementCacheQueries = options.getValue(Option.valueOf<String>("preparedStatementCacheQueries")) as String?
        val sslMode = options.getValue(Option.valueOf<String>("sslMode")) as String?
        val socket = options.getValue(Option.valueOf<String>("socket")) as String?
        val applicationName = options.getValue(Option.valueOf<String>("applicationName")) as String?
        val autodetectExtensions = options.getValue(Option.valueOf<String>("autodetectExtensions")) as String?
        val tcpKeepAlive = options.getValue(Option.valueOf<String>("tcpKeepAlive")) as String?
        val tcpNoDelay = options.getValue(Option.valueOf<String>("tcpNoDelay")) as String?

        var builder =
            PostgresqlConnectionConfiguration.builder()
                .database(database)
                .username(prop.username)
                .password(prop.password)
                .host(host)
                .port(port)

        preparedStatementCacheQueries?.let { builder = builder.preparedStatementCacheQueries(it.toInt()) }
        sslMode?.let { builder = builder.sslMode(SSLMode.valueOf(it)) }
        socket?.let { builder = builder.socket(it) }
        applicationName?.let { builder = builder.applicationName(it) }
        autodetectExtensions?.let { builder = builder.autodetectExtensions(it.toBoolean()) }
        tcpKeepAlive?.let { builder = builder.tcpKeepAlive(it.toBoolean()) }
        tcpNoDelay?.let { builder = builder.tcpNoDelay(it.toBoolean()) }

        findFieldsWithAnnotation<PgEnum>().forEach { (field, annotation) ->
            @Suppress("UNCHECKED_CAST")
            val registrar = buildCodecRegistrar(annotation.name, field.baseType() as Class<out Enum<*>>)
            builder = builder.codecRegistrar(registrar)
        }

        val postgresConnection = PostgresqlConnectionFactory(builder.build())
        return if (prop.pool.isEnabled) {
            val poolConfig =
                ConnectionPoolConfiguration.builder()
                    .connectionFactory(postgresConnection)
                    .initialSize(prop.pool.initialSize)
                    .maxSize(prop.pool.maxSize)
                    .maxAcquireTime(prop.pool.maxAcquireTime)
                    .maxIdleTime(prop.pool.maxIdleTime)
                    .maxLifeTime(prop.pool.maxLifeTime)
                    .maxCreateConnectionTime(prop.pool.maxCreateConnectionTime)
                    .validationDepth(prop.pool.validationDepth)

            logger.info("Initializing database pool config {}", poolConfig)
            ConnectionPool(poolConfig.build())
        } else {
            logger.info("Initializing database config {}", builder.build())
            postgresConnection
        }
    }

    override fun getCustomConverters(): List<Any> {
        val enumConverters =
            findFieldsWithAnnotation<PgEnum>().map { (field, _) ->
                CustomPostgresEnumConverter(field.baseType())
            }
        val jsonConverters =
            findFieldsWithAnnotation<PgJson>().filter {
                it.second.createConverters
            }.filter {
                !CustomCollections.isMap(it.first.type)
            }.map { (field, _) ->
                listOf(
                    CustomPostgresJsonWritingConverter(field.type, objectMapper),
                    CustomPostgresJsonReadingConverter(field, objectMapper),
                )
            }.flatten()

        val jsonToMapConverters = listOf(JsonToMapConverter(objectMapper), MapToJsonConverter(objectMapper))

        return converters +
            enumConverters +
            jsonConverters +
            jsonToMapConverters +
            DurationToIntervalConverter() +
            IntervalToDurationConverter()
    }

    override fun getMappingBasePackages(): MutableCollection<String> {
        val candidates = context.getBeansWithAnnotation(SpringBootApplication::class.java)
        return candidates.map { it.value::class.java.packageName }.toMutableList().also {
            logger.info("r2dbc table packages: {}", it)
        }
    }

    private inline fun <reified T : Annotation> findFieldsWithAnnotation(): List<Pair<Field, T>> {
        return r2dbcManagedTypes().toList()
            .mapNotNull { context.classLoader?.loadClass(it.name) ?: it }
            .flatMap {
                it.declaredFields.mapNotNull { field ->
                    val annotation = field.getDeclaredAnnotation(T::class.java) ?: return@mapNotNull null
                    field to annotation
                }
            }
    }

    private fun Field.isCollection(): Boolean {
        return Collection::class.java.isAssignableFrom(this.type)
    }

    private fun Field.elementType(): Class<*> {
        return when (val type = this.genericType) {
            is Class<*> -> type.componentType
            is ParameterizedType -> {
                check(type.actualTypeArguments.count() == 1) { "Invalid actualTypeArguments count" }
                type.actualTypeArguments.first() as Class<*>
            }

            else -> throw IllegalStateException("Unhandled entity class $type")
        }
    }

    private fun Field.baseType(): Class<*> {
        return if (this.isCollection()) {
            this.elementType()
        } else {
            this.type
        }
    }

    private fun buildCodecRegistrar(
        name: String,
        javaClass: Class<out Enum<*>>,
    ): CodecRegistrar {
        return EnumCodec.builder().withEnum(name, javaClass).build()
    }
}

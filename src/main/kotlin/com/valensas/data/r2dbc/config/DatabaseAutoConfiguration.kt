package com.valensas.data.r2dbc.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.valensas.data.r2dbc.annotation.PgEnum
import com.valensas.data.r2dbc.annotation.PgJson
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.client.SSLMode
import io.r2dbc.postgresql.codec.EnumCodec
import io.r2dbc.postgresql.codec.Json
import io.r2dbc.postgresql.extension.CodecRegistrar
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.Option
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.core.convert.converter.GenericConverter.ConvertiblePair
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.relational.core.mapping.Table

@Configuration
@EnableConfigurationProperties(R2dbcProperties::class)
class DatabaseAutoConfiguration(
    private val prop: R2dbcProperties,
    private val context: ApplicationContext,
    private val objectMapper: ObjectMapper
) : AbstractR2dbcConfiguration() {
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

        var builder = PostgresqlConnectionConfiguration.builder()
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

        findFieldsWithAnnotation<PgEnum>().forEach { (enumClass, annotation) ->
            if (!enumClass.isEnum) return@forEach
            val registrar = buildCodecRegistrar(annotation.name, enumClass as Class<out Enum<*>>)
            builder = builder.codecRegistrar(registrar)
        }

        val postgresConnection = PostgresqlConnectionFactory(builder.build())
        return if (prop.pool.isEnabled) {
            val poolConfig = ConnectionPoolConfiguration.builder()
                .connectionFactory(postgresConnection)
                .initialSize(prop.pool.initialSize)
                .maxSize(prop.pool.maxSize)
                .maxAcquireTime(prop.pool.maxAcquireTime)
                .maxIdleTime(prop.pool.maxIdleTime)
                .maxLifeTime(prop.pool.maxLifeTime)
                .maxCreateConnectionTime(prop.pool.maxCreateConnectionTime)
                .validationDepth(prop.pool.validationDepth)
                .build()
            ConnectionPool(poolConfig)
        } else {
            postgresConnection
        }
    }

    @Bean
    override fun getCustomConverters(): List<GenericConverter> {
        val enumConverters = findFieldsWithAnnotation<PgEnum>().map { (type, _) ->
            @WritingConverter
            class CustomPostgresEnumConverter : GenericConverter {
                override fun getConvertibleTypes(): Set<ConvertiblePair> {
                    return setOf(ConvertiblePair(type, type))
                }

                override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
                    return source
                }
            }

            val converter = CustomPostgresEnumConverter()
            converter
        }

        val jsonConverters = findFieldsWithAnnotation<PgJson>().map { (type, _) ->
            @WritingConverter
            class CustomPostgresJsonWritingConverter : GenericConverter {
                override fun getConvertibleTypes(): Set<ConvertiblePair> {
                    return setOf(ConvertiblePair(type, Json::class.java))
                }

                override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
                    return source?.let { Json.of(objectMapper.writeValueAsString(it)) }
                }
            }

            @ReadingConverter
            class CustomPostgresJsonReadingConverter : GenericConverter {
                override fun getConvertibleTypes(): Set<ConvertiblePair> {
                    return setOf(ConvertiblePair(Json::class.java, type))
                }

                override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
                    return objectMapper.readValue((source as Json).asArray(), targetType.type)
                }
            }

            listOf(CustomPostgresJsonWritingConverter(), CustomPostgresJsonReadingConverter())
        }.flatten()

        return enumConverters + jsonConverters
    }

    private fun findProjectPackage(): List<String> {
        val candidates = context.getBeansWithAnnotation(SpringBootApplication::class.java)
        return candidates.map { it.value::class.java.packageName }
    }

    private inline fun <reified T : Annotation> findFieldsWithAnnotation(): List<Pair<Class<*>, T>> {
        val packages = findProjectPackage()

        val scanner = ClassPathScanningCandidateComponentProvider(false)
        scanner.addIncludeFilter(AnnotationTypeFilter(Table::class.java))
        val tables = packages.flatMap { scanner.findCandidateComponents(it) }

        return tables.flatMap {
            val entityClass = context.classLoader!!.loadClass(it.beanClassName)
            entityClass.declaredFields.mapNotNull { field ->
                val annotation = field.getDeclaredAnnotation(T::class.java) ?: return@mapNotNull null
                field.type to annotation
            }
        }
    }

    private fun buildCodecRegistrar(name: String, javaClass: Class<out Enum<*>>): CodecRegistrar {
        return EnumCodec.builder().withEnum(name, javaClass).build()
    }
}

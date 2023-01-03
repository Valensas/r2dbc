package com.valensas.data.r2dbc.config

import com.valensas.data.r2dbc.annotation.PgEnum
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.codec.EnumCodec
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
import org.springframework.data.convert.WritingConverter
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.relational.core.mapping.Table

@Configuration
@EnableConfigurationProperties(R2dbcProperties::class)
class DatabaseAutoConfiguration(
    private val prop: R2dbcProperties,
    private val context: ApplicationContext
) : AbstractR2dbcConfiguration() {
    @Bean
    override fun connectionFactory(): ConnectionFactory {
        val options = ConnectionFactoryOptions.parse(prop.url)
        val host = options.getValue(Option.valueOf<String>("host")) as String
        val port = options.getValue(Option.valueOf<String>("port")) as Int
        val database = options.getValue(Option.valueOf<String>("database")) as String

        var builder = PostgresqlConnectionConfiguration.builder()
            .database(database)
            .username(prop.username)
            .password(prop.password)
            .host(host)
            .port(port)
            .preparedStatementCacheQueries(0)

        findFieldsWithPgEnumAnnotation().forEach { (type, annotation) ->
            val enumClass = ClassLoader.getSystemClassLoader().loadClass(type)
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
        return findFieldsWithPgEnumAnnotation().map { (type, _) ->
            @WritingConverter
            class CustomPostgresEnumConverter : GenericConverter {
                override fun getConvertibleTypes(): Set<ConvertiblePair> {
                    val enumClass = ClassLoader.getSystemClassLoader().loadClass(type)
                    return setOf(
                        ConvertiblePair(enumClass, enumClass)
                    )
                }

                override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
                    return source
                }
            }

            val converter = CustomPostgresEnumConverter()
            converter
        }
    }

    private fun findProjectPackage(): List<String> {
        val candidates = context.getBeansWithAnnotation(SpringBootApplication::class.java)
        return candidates.map { it.value::class.java.packageName }
    }

    private fun findFieldsWithPgEnumAnnotation(): List<Pair<String, PgEnum>> {
        val packages = findProjectPackage()

        val scanner = ClassPathScanningCandidateComponentProvider(false)
        scanner.addIncludeFilter(AnnotationTypeFilter(Table::class.java))
        val tables = packages.flatMap { scanner.findCandidateComponents(it) }

        return tables.flatMap {
            val entityClass = ClassLoader.getSystemClassLoader().loadClass(it.beanClassName)
            entityClass.declaredFields.mapNotNull { field ->
                val annotation = field.getDeclaredAnnotation(PgEnum::class.java) ?: return@mapNotNull null
                field.type.name to annotation
            }
        }
    }

    private fun buildCodecRegistrar(name: String, javaClass: Class<out Enum<*>>): CodecRegistrar {
        return EnumCodec.builder().withEnum(name, javaClass).build()
    }
}

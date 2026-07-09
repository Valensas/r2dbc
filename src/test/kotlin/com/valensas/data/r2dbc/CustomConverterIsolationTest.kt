package com.valensas.data.r2dbc

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.core.convert.converter.Converter
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@Import(CustomConverterIsolationTest.UnannotatedConverterConfiguration::class)
class CustomConverterIsolationTest(
    @Autowired
    private val r2dbcConverter: MappingR2dbcConverter
) {
    class UnannotatedMapConverter : Converter<String, Map<String, String>> {
        override fun convert(source: String): Map<String, String> = source.split(",").associate { entry ->
            val parts = entry.split("=", limit = 2)
            require(parts.size == 2) { "Invalid map property: $entry" }
            parts[0] to parts[1]
        }
    }

    @TestConfiguration
    class UnannotatedConverterConfiguration {
        @Bean
        fun unannotatedMapConverter(): Converter<String, Map<String, String>> = UnannotatedMapConverter()
    }

    @Test
    fun `unannotated context converter is not adopted into r2dbc conversions`() {
        assertFalse(r2dbcConverter.conversionService.canConvert(String::class.java, Map::class.java))
    }
}

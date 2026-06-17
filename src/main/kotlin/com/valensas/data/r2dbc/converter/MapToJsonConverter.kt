package com.valensas.data.r2dbc.converter

import com.fasterxml.jackson.databind.ObjectMapper
import io.r2dbc.postgresql.codec.Json
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter

@WritingConverter
class MapToJsonConverter(
    private val objectMapper: ObjectMapper,
) : Converter<Map<Any, Any?>, Json> {
    override fun convert(source: Map<Any, Any?>): Json = source.let { Json.of(objectMapper.writeValueAsString(it)) }
}

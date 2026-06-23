package com.valensas.data.r2dbc.converter

import io.r2dbc.postgresql.codec.Json
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper

@ReadingConverter
class JsonToMapConverter(
    private val objectMapper: ObjectMapper
) : Converter<Json, Map<Any, Any?>> {
    override fun convert(source: Json): Map<Any, Any?> {
        val typeRef: TypeReference<Map<Any, Any?>> = object : TypeReference<Map<Any, Any?>>() {}
        return objectMapper.readValue(source.asArray(), typeRef)
    }
}

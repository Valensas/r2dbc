package com.valensas.data.r2dbc.converter

import io.r2dbc.postgresql.codec.Json
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.data.convert.WritingConverter
import tools.jackson.databind.ObjectMapper

@WritingConverter
class CustomPostgresJsonWritingConverter(
    private val type: Class<*>,
    private val objectMapper: ObjectMapper
) : GenericConverter {
    override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair> = setOf(GenericConverter.ConvertiblePair(type, Json::class.java))

    override fun convert(
        source: Any?,
        sourceType: TypeDescriptor,
        targetType: TypeDescriptor
    ): Any? = source?.let { Json.of(objectMapper.writeValueAsString(it)) }
}

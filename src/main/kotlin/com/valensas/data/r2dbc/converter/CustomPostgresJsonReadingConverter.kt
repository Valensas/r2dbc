package com.valensas.data.r2dbc.converter

import com.fasterxml.jackson.databind.ObjectMapper
import io.r2dbc.postgresql.codec.Json
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.data.convert.ReadingConverter
import java.lang.reflect.Field

@ReadingConverter
class CustomPostgresJsonReadingConverter(
    private val field: Field,
    private val objectMapper: ObjectMapper
) : GenericConverter {
    override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair> {
        return setOf(GenericConverter.ConvertiblePair(Json::class.java, field.type))
    }

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        return objectMapper.readValue((source as Json).asArray(), objectMapper.typeFactory.constructType(field.genericType))
    }
}

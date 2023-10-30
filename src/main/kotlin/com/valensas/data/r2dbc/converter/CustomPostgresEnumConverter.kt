package com.valensas.data.r2dbc.converter

import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.data.convert.WritingConverter

@WritingConverter
class CustomPostgresEnumConverter(
    private val type: Class<*>,
) : GenericConverter {
    override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair> {
        return setOf(GenericConverter.ConvertiblePair(type, type))
    }

    override fun convert(
        source: Any?,
        sourceType: TypeDescriptor,
        targetType: TypeDescriptor,
    ): Any? {
        return source
    }
}

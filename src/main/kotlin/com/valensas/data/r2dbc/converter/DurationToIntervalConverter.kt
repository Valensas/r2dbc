package com.valensas.data.r2dbc.converter

import io.r2dbc.postgresql.codec.Interval
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.data.convert.WritingConverter
import java.time.Duration

@WritingConverter
class DurationToIntervalConverter : GenericConverter {
    override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair> {
        return setOf(GenericConverter.ConvertiblePair(Duration::class.java, Interval::class.java))
    }

    override fun convert(
        source: Any?,
        sourceType: TypeDescriptor,
        targetType: TypeDescriptor,
    ): Any {
        return (source as Duration).let { Interval.of(it) }
    }
}

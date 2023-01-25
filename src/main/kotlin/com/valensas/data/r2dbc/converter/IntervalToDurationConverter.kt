package com.valensas.data.r2dbc.converter

import io.r2dbc.postgresql.codec.Interval
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.data.convert.ReadingConverter
import java.time.Duration

@ReadingConverter
class IntervalToDurationConverter : GenericConverter {
    override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair> {
        return setOf(GenericConverter.ConvertiblePair(Interval::class.java, Duration::class.java))
    }

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any {
        return (source as Interval).duration
    }
}

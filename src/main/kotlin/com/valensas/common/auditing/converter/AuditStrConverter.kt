package com.valensas.common.auditing.converter

import com.valensas.common.auditing.model.AuditStr
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class AuditStrConverter : AttributeConverter<AuditStr?, String?> {
    override fun convertToDatabaseColumn(auditStr: AuditStr?): String? {
        return auditStr?.let { """${auditStr.clientId}|${auditStr.username}|${auditStr.deviceId}|${auditStr.remoteAddress}""" }
    }

    override fun convertToEntityAttribute(dbData: String?): AuditStr? {
        val items = dbData?.split('|')
        return items?.let {
            AuditStr(
                items.getOrElse(3) { "" },
                items.getOrElse(2) { "" },
                items.getOrElse(1) { "" },
                items.getOrElse(0) { "" }
            )
        }
    }
}
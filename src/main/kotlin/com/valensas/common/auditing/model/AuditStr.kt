package com.valensas.common.auditing.model

import java.io.Serializable

data class AuditStr(
    val remoteAddress: String,
    val deviceId: String,
    val username: String,
    val clientId: String
) : Serializable
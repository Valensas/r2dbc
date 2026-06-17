package com.valensas.data.r2dbc.model

import java.net.InetAddress

data class Audit(
    val username: String,
    val ip: InetAddress
)

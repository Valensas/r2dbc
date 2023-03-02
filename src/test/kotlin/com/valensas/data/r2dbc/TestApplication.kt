package com.valensas.data.r2dbc

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class TestApplication {
    @Bean
    fun objectMapper() = ObjectMapper()
}

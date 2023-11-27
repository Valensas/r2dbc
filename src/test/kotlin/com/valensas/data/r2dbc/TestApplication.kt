package com.valensas.data.r2dbc

import com.valensas.data.r2dbc.entity.EnumEntity
import com.valensas.data.r2dbc.repository.EnumEntityRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@SpringBootApplication
class TestApplication

@RestController
class TestController(
    private val enumEntityRepository: EnumEntityRepository,
) {
    @PostMapping("/")
    fun test(): Mono<EnumEntity> {
        val entity =
            EnumEntity(
                type = EnumEntity.Type.Type1,
            )
        return enumEntityRepository.save(entity)
    }
}

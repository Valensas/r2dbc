package com.valensas.data.r2dbc

import com.valensas.data.r2dbc.entity.EnumEntity
import com.valensas.data.r2dbc.entity.JsonEntity
import com.valensas.data.r2dbc.repository.EnumEntityRepository
import com.valensas.data.r2dbc.repository.JsonEntityRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseTest(
    @Autowired
    private val enumEntityRepository: EnumEntityRepository,
    @Autowired
    private val jsonEntityRepository: JsonEntityRepository,
    @Autowired
    private val webTestClient: WebTestClient
) {
    @BeforeEach
    fun cleanup(): Unit = runBlocking {
        enumEntityRepository.deleteAll().awaitFirstOrNull()
        jsonEntityRepository.deleteAll().awaitFirstOrNull()
    }

    @Test
    fun canWriteAndReadEnum() = runBlocking {
        val entity =
            EnumEntity(
                type = EnumEntity.Type.Type1
            )
        val savedEntity = enumEntityRepository.save(entity).awaitSingle()
        assertEquals(entity.type, savedEntity.type)

        val readEntity = enumEntityRepository.findById(savedEntity.id!!).awaitSingle()
        assertEquals(entity.type, readEntity.type)
    }

    @Test
    fun canWriteAndReadJson() = runBlocking {
        val entity =
            JsonEntity(
                data =
                mapOf(
                    "key1" to "value1",
                    "key2" to "value2"
                )
            )
        val savedEntity = jsonEntityRepository.save(entity).awaitSingle()
        assertEquals(entity.data, savedEntity.data)

        val readEntity = jsonEntityRepository.findById(savedEntity.id!!).awaitSingle()
        assertEquals(entity.data, readEntity.data)
    }

    @Test
    fun canAudit() {
        val response =
            webTestClient
                .post()
                .uri("/")
                .exchange()
                .expectStatus()
                .is2xxSuccessful
                .expectBody(EnumEntity::class.java)
                .returnResult()
                .responseBody!!

        assertNotNull(response.createdBy)
        assertNotNull(response.createdDate)
    }
}

package com.valensas.data.r2dbc.repository

import com.valensas.data.r2dbc.entity.JsonEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface JsonEntityRepository : ReactiveCrudRepository<JsonEntity, Long>

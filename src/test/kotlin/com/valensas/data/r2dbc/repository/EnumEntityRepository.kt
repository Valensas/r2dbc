package com.valensas.data.r2dbc.repository

import com.valensas.data.r2dbc.entity.EnumEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface EnumEntityRepository : ReactiveCrudRepository<EnumEntity, Long>

package com.valensas.data.exposed.repository

import com.valensas.data.exposed.entity.TestRecord
import com.valensas.data.exposed.entity.TestTable
import org.springframework.stereotype.Repository

@Repository
class TestEntityRepository : CrudRepository<TestRecord, Long>(TestTable)

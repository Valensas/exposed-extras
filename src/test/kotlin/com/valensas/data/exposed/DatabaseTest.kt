package com.valensas.data.exposed

import com.valensas.data.exposed.entity.TestRecord
import com.valensas.data.exposed.entity.TestTable
import com.valensas.data.exposed.entity.UniqueRecord
import com.valensas.data.exposed.entity.UniqueTable
import com.valensas.data.exposed.repository.TestEntityRepository
import com.valensas.data.exposed.util.extension.postgres.upsert
import com.valensas.data.exposed.util.extension.postgres.upsertAndGetId
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.net.InetAddress
import java.time.Duration

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseTest(
    @Autowired
    private val testEntityRepository: TestEntityRepository
) {
    @BeforeEach
    fun cleanup() {
        transaction {
            TestTable.deleteAll()
            UniqueTable.deleteAll()
            commit()
        }
    }

    @Test
    fun `can create, update, select, delete record`() {
        val entity1 =
            TestRecord(
                type = TestRecord.Type.Type1,
                testJson = TestRecord.SomeModel("h1"),
                interval = Duration.ofMinutes(5000),
                ip = InetAddress.getLocalHost(),
                integers = listOf(1, 2, 3)
            ).let { it.copy(id = testEntityRepository.insert(it)) }

        val entity2 =
            TestRecord(
                type = TestRecord.Type.Type2,
                testJson = TestRecord.SomeModel("h2"),
                interval = Duration.ofDays(2).plusMinutes(123),
                ip = InetAddress.getLoopbackAddress(),
                integers = listOf(4, 5, 6)
            ).let { it.copy(id = testEntityRepository.insert(it)) }

        val entities = testEntityRepository.findAll()
        assertEquals(listOf(entity1, entity2).sortedBy { it.id }, entities.sortedBy { it.id })

        var retrievedEntity = testEntityRepository.findOneById(entity1.id!!)
        assertNotNull(retrievedEntity)
        assertEquals(TestRecord.Type.Type1, retrievedEntity?.type)

        testEntityRepository.update(
            entity1.id,
            entity1.copy(type = TestRecord.Type.Type2)
        )
        retrievedEntity = testEntityRepository.findOneById(entity1.id)
        assertNotNull(retrievedEntity)
        assertEquals(TestRecord.Type.Type2, retrievedEntity?.type)

        testEntityRepository.deleteById(entity2.id!!)
        assertEquals(1, testEntityRepository.findAll().size)
    }

    @Test
    fun `Can upsert`() {
        var record = UniqueRecord(id = null, name = "something", value = 0)
        val recordId =
            transaction {
                val recordId =
                    UniqueTable.upsertAndGetId(
                        conflictColumns = arrayOf(UniqueTable.name),
                        updateOnConflict = false
                    ) {
                        it[name] = record.name
                        it[value] = record.value
                    }
                commit()
                recordId
            }!!

        record = record.copy(value = 1)
        transaction {
            UniqueTable.upsert(
                conflictColumns = arrayOf(UniqueTable.name),
                updateOnConflict = true,
                updateColumns = arrayOf(UniqueTable.value)
            ) {
                it[name] = record.name
                it[value] = record.value
            }

            val savedRecord = UniqueTable.findOneById(recordId)!!
            assertEquals(record.value, savedRecord.value)
            commit()
        }
    }
}

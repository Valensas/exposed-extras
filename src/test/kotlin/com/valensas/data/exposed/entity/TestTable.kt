package com.valensas.data.exposed.entity

import com.valensas.data.exposed.table.AuditableTable
import com.valensas.data.exposed.util.extension.array
import com.valensas.data.exposed.util.extension.enum
import com.valensas.data.exposed.util.extension.inet
import com.valensas.data.exposed.util.extension.interval
import com.valensas.data.exposed.util.extension.jsonb
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import java.net.InetAddress
import java.time.Duration

object TestTable : AuditableTable<TestRecord, Long, String>(
    tableName = "test_table",
    defaultPrincipal = "anonymous"
) {
    override val id: Column<EntityID<Long>> = long("id").entityId()
    override val createdBy: Column<String> = text("created_by")
    override val updatedBy: Column<String?> = text("updated_by").nullable()
    private val type = enum<TestRecord.Type>("type", "test_table_type")
    private val testJson = jsonb<TestRecord.SomeModel>("test_json")
    private val testArray = array<Int>("test_array", IntegerColumnType())
    private val testInet = inet("test_inet")
    private val testInterval = interval("test_interval")

    override fun rowToRecord(row: ResultRow): TestRecord =
        TestRecord(
            id = row[id].value,
            type = row[type],
            testJson = row[testJson],
            integers = row[testArray].toList(),
            ip = row[testInet],
            interval = row[testInterval]
        )

    override fun updateRowWithRecord(
        statement: UpdateStatement,
        record: TestRecord
    ) {
        statement[type] = record.type
        statement[testJson] = record.testJson
        statement[testArray] = record.integers.toTypedArray()
        statement[testInet] = record.ip
        statement[testInterval] = record.interval
        super.updateRowWithRecord(statement, record)
    }

    override fun insertRowWithRecord(
        statement: InsertStatement<EntityID<Long>>,
        record: TestRecord
    ) {
        statement[type] = record.type
        statement[testJson] = record.testJson
        statement[testArray] = record.integers.toTypedArray()
        statement[testInet] = record.ip
        statement[testInterval] = record.interval
        super.insertRowWithRecord(statement, record)
    }
}

data class TestRecord(
    val id: Long? = null,
    val type: Type,
    val testJson: SomeModel,
    val integers: List<Int>,
    val ip: InetAddress,
    val interval: Duration
) {
    enum class Type {
        Type1,
        Type2
    }

    data class SomeModel(
        val test: String
    )
}

package com.valensas.data.exposed.entity

import com.valensas.data.exposed.table.CrudTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

object UniqueTable : CrudTable<UniqueRecord, Long>("unique_table") {
    override val id: Column<EntityID<Long>> = long("id").entityId().databaseGenerated()
    val name = text("name").uniqueIndex("idx_unique_table_on_name")
    val value = integer("value")

    override fun rowToRecord(row: ResultRow): UniqueRecord {
        return UniqueRecord(
            id = row[id].value,
            name = row[name],
            value = row[value]
        )
    }

    override fun updateRowWithRecord(
        statement: UpdateStatement,
        record: UniqueRecord
    ) {
        statement[name] = record.name
        statement[value] = record.value
    }

    override fun insertRowWithRecord(
        statement: InsertStatement<EntityID<Long>>,
        record: UniqueRecord
    ) {
        statement[name] = record.name
        statement[value] = record.value
    }
}

data class UniqueRecord(
    val id: Long?,
    val name: String,
    val value: Int
)

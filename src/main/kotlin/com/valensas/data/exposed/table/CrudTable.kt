package com.valensas.data.exposed.table

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.update

/**
 * A table that implements CRUD operations on the database.
 *
 * @param Record The record type for the table
 * @param ID The id type for the table
 * @param tableName The name of the database table
 */
abstract class CrudTable<Record : Any, ID : Comparable<ID>>(
    tableName: String
) : IdTable<ID>(tableName), ICrudTable<Record, ID> {
    /**
     * Transforms a database row to a record. This method is called
     * upon performing SELECT queries.
     *
     * @param row The row to convert
     * @return The record representing the row
     */
    abstract fun rowToRecord(row: ResultRow): Record

    /**
     * Writes the SET clause for an INSERT database query.
     *
     * @param statement The insert statement
     * @param record The record to be inserted
     */
    abstract fun insertRowWithRecord(
        statement: InsertStatement<EntityID<ID>>,
        record: Record
    )

    /**
     * Writes the SET clause for an UPDATE database query.
     *
     * @param statement The update statement
     * @param record The record to be updated
     */
    abstract fun updateRowWithRecord(
        statement: UpdateStatement,
        record: Record
    )

    override fun findOneById(
        id: ID,
        forUpdate: Boolean
    ): Record? {
        var query =
            this.selectAll().where { this@CrudTable.id eq id }
                .limit(1)

        query = if (forUpdate) query.forUpdate() else query.notForUpdate()
        return query
            .mapNotNull { this.rowToRecord(it) }
            .firstOrNull()
    }

    override fun findAll(forUpdate: Boolean): List<Record> {
        var query = this.selectAll()
        query = if (forUpdate) query.forUpdate() else query.notForUpdate()

        return query.mapNotNull(::rowToRecord)
    }

    override fun update(
        id: ID,
        record: Record
    ): Int {
        return update({ this@CrudTable.id eq id }) {
            updateRowWithRecord(it, record)
        }
    }

    override fun insert(record: Record): ID {
        return insertAndGetId {
            insertRowWithRecord(it, record)
        }.value
    }

    override fun deleteById(id: ID): Int {
        return deleteWhere { this@CrudTable.id eq id }
    }
}

package com.valensas.data.exposed.table

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import java.time.Instant

/**
 * A table that implements auditing in addition to CRUD operations. It keeps
 * track of the following auditing information:
 *
 * - the time at which the row was created and updated
 * - the principal (user) that created and updated the row
 *
 * Note that auditing information is only filled when using methods from
 * {@link  com.valensas.data.exposed.table#ICrudTable ICrudTable}. Other
 * queries will need to fill auditing data manually if required.
 *
 * @param Record The record type for the table
 * @param ID The id type for the table
 * @param Principal The principal type of the auditing columns
 * @param tableName The name of the database table
 * @param defaultPrincipal The default principal to use for creator and updater information
 * @param createdDateColumnName The name of the column tracking the insert date
 * @param updatedDateColumnName The name of the column tracking the last update date
 */
abstract class AuditableTable<Record : Any, ID : Comparable<ID>, Principal>(
    tableName: String,
    private val defaultPrincipal: Principal,
    createdDateColumnName: String = "created_date",
    updatedDateColumnName: String = "updated_date"
) : CrudTable<Record, ID>(tableName) {
    override val primaryKey by lazy { super.primaryKey ?: PrimaryKey(id) }
    open val createdDate = timestamp(createdDateColumnName).clientDefault(Instant::now)
    abstract val createdBy: Column<Principal>
    open val updatedDate = timestamp(updatedDateColumnName).nullable()
    abstract val updatedBy: Column<Principal?>

    /**
     * Get the principal acting the record for an insert or update. Returns
     * the default principal by default. Override the method to customize it.
     *
     * @param record The record that is being inserted or updated
     * @return The principal performing the action
     */
    open fun getPrincipal(record: Record): Principal = defaultPrincipal

    override fun updateRowWithRecord(
        statement: UpdateStatement,
        record: Record
    ) {
        statement[updatedBy] = getPrincipal(record)
        statement[updatedDate] = Instant.now()
    }

    override fun insertRowWithRecord(
        statement: InsertStatement<EntityID<ID>>,
        record: Record
    ) {
        statement[createdBy] = getPrincipal(record)
        statement[createdDate] = Instant.now()
    }
}

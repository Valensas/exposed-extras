package com.valensas.data.exposed.repository

import com.valensas.data.exposed.table.ICrudTable
import org.springframework.transaction.annotation.Transactional

/**
 * A repository that implements CRUD operations for a given table.
 *
 * @param Record The record type for the table
 * @param ID The id type for the table
 * @param table The table to perform CRUD operation on
 */
@Transactional
abstract class CrudRepository<Record : Any, ID : Comparable<ID>>(
    private val table: ICrudTable<Record, ID>
) {
    /**
     * Inserts a record into the database.
     *
     * @param record The record to insert
     * @return The id of the newly inserted record
     */
    fun insert(record: Record) = table.insert(record)

    /**
     * Selects a record from the database by id, optionally using FOR UPDATE clause.
     *
     * @param id The id of the row to select
     * @param forUpdate When set to true, uses a FOR UPDATE lock to perform pessimistic locking
     * @return The record from the database
     */
    fun findOneById(
        id: ID,
        forUpdate: Boolean = false
    ) = table.findOneById(id, forUpdate)

    /**
     * Selects all records from the database, optionally using FOR UPDATE clause.
     *
     * @param forUpdate When set to true, uses a FOR UPDATE lock to perform pessimistic locking
     * @return All records from the database
     */
    fun findAll(forUpdate: Boolean = false) = table.findAll(forUpdate)

    /**
     * Updates a record by its id.
     *
     * @param id The id of the row to update
     * @param record The parameter to update
     * @return The number of rows updated.
     */
    fun update(
        id: ID,
        record: Record
    ) = table.update(id, record)

    /**
     * Deletes a row by its id.
     *
     * @param id The id of the row to update
     * @return The number of rows updated.
     */
    fun deleteById(id: ID) = table.deleteById(id)
}

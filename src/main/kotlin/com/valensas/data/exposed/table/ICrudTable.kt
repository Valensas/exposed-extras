package com.valensas.data.exposed.table

interface ICrudTable<Record : Any, ID : Comparable<ID>> {
    /**
     * Inserts a record into the database.
     *
     * @param record The record to insert
     * @return The id of the newly inserted record
     */
    fun insert(record: Record): ID

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
    ): Record?

    /**
     * Selects all records from the database, optionally using FOR UPDATE clause.
     *
     * @param forUpdate When set to true, uses a FOR UPDATE lock to perform pessimistic locking
     * @return All records from the database
     */
    fun findAll(forUpdate: Boolean = false): List<Record>

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
    ): Int

    /**
     * Deletes a row by its id.
     *
     * @param id The id of the row to update
     * @return The number of rows updated.
     */
    fun deleteById(id: ID): Int
}

package com.valensas.data.exposed.util.extension.postgres

import com.valensas.data.exposed.util.statement.postgres.UpsertStatement
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Index
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.TransactionManager

/**
 * Upsert statement, updates or skips if conflict is detected.
 *
 * @see <a href="https://www.postgresql.org/docs/current/sql-insert.html#SQL-ON-CONFLICT">PostgresSQL INSERT ON CONFLICT</a>
 * @param conflictColumns Conflicted columns
 * @param conflictIndex Conflicted index
 *                      Please use conflictedColumns or conflictIndex,
 *                      UpsertStatement is looking for columns first
 *                      so conflictIndex will be ignored if both of them is present
 * @param conflictPredicate The index_predicate for partial indexes, only supported with columns usage
 * @param updateColumns The update columns when updateOnConflict==true;
 *                      note that conflicted columns will be removed from the list
 *                      if it is empty only the insertStatement columns will be updated
 * @param updateOnConflict whether to update or skip if there is a conflict
 * @param body insert statement of the given entity and record
 * @return Updated Row count,
 */
inline fun <Key : Comparable<Key>, T : IdTable<Key>> T.upsert(
    vararg conflictColumns: Column<*> = arrayOf(),
    conflictIndex: Index? = null,
    conflictPredicate: Op<Boolean>? = null,
    updateColumns: Array<Column<*>> = arrayOf(),
    updateOnConflict: Boolean = false,
    body: T.(InsertStatement<EntityID<Key>>) -> Unit
): Int =
    UpsertStatement<EntityID<Key>>(
        this,
        conflictIndex,
        conflictColumns,
        conflictPredicate,
        updateColumns,
        updateOnConflict
    ).run {
        body(this)
        execute(TransactionManager.current()) ?: 0
    }

/**
* upsert statement, updates or skips if there is given conflict
* @param conflictColumns Conflicted columns
* @param conflictIndex Conflicted index
*                      Please use conflictedColumns or conflictIndex,
*                      UpsertStatement is looking for columns first
*                      so conflictIndex will be ignored if both of them is present
* @param conflictPredicate The index_predicate for partial indexes, only supported with columns usage
* @param updateColumns The update columns when updateOnConflict==true;
*                      note that conflicted columns will be removed from the list
*                      if it is empty only the insertStatement columns will be updated
* @param updateOnConflict whether to update or skip if there is a conflict
* @param body insert statement of the given entity and record
* @return The updated id of the row or null if @updateOnConflict==false,
*/
inline fun <Key : Comparable<Key>, T : IdTable<Key>> T.upsertAndGetId(
    vararg conflictColumns: Column<*> = arrayOf(),
    conflictIndex: Index? = null,
    conflictPredicate: Op<Boolean>? = null,
    updateColumns: Array<Column<*>> = arrayOf(),
    updateOnConflict: Boolean = false,
    body: T.(InsertStatement<EntityID<Key>>) -> Unit
) = UpsertStatement<EntityID<Key>>(
    this,
    conflictIndex,
    conflictColumns,
    conflictPredicate,
    updateColumns,
    updateOnConflict
).run {
    body(this)
    execute(TransactionManager.current())
        ?.takeIf { it > 0 }
        ?.let { get(id).value }
}

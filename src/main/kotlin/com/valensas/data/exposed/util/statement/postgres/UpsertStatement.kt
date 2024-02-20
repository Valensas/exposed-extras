package com.valensas.data.exposed.util.statement.postgres

import com.valensas.data.exposed.util.extension.postgres.toIndexPredicateBuilder
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Index
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.InsertStatement

/**
 * Supported for only postgres with version > 9.5
 * Does not support complex expressions as index_predicate
 */
class UpsertStatement<Key : Any>(
    table: Table,
    conflictConstraint: Index? = null,
    conflictColumns: Array<out Column<*>> = arrayOf(),
    private val conflictPredicate: Op<Boolean>? = null,
    updateColumns: Array<out Column<*>> = arrayOf(),
    private val updateOnConflict: Boolean = false
) : InsertStatement<Key>(table, false) {
    private val conflictIndexName: String
    private val conflictedColumns: List<Column<*>>
    private val constraint: Boolean
    private val toBeUpdatedColumns: List<Column<*>>

    init {
        when {
            conflictColumns.isNotEmpty() -> {
                constraint = false
                conflictIndexName = ""
                conflictedColumns = conflictColumns.toList()
            }
            conflictConstraint != null -> {
                constraint = true
                conflictIndexName = conflictConstraint.indexName
                conflictedColumns = conflictConstraint.columns
                conflictPredicate?.let { throw IllegalArgumentException("conflictPredicate with constraint name is not supported") }
            }
            else -> throw IllegalArgumentException()
        }

        toBeUpdatedColumns =
            when {
                !updateOnConflict -> emptyList()
                updateColumns.isNotEmpty() -> updateColumns.toList()
                else -> table.columns
            }.filter { it !in conflictedColumns }
    }

    override fun prepareSQL(
        transaction: Transaction,
        prepared: Boolean
    ) = buildString {
        append(super.prepareSQL(transaction, prepared))
        if (constraint) {
            append(" ON CONFLICT ON CONSTRAINT ")
            append(conflictIndexName)
        } else {
            append(" ON CONFLICT(")
            append(conflictedColumns.joinToString(separator = ", ", transform = { it.name }))
            append(")")
            conflictPredicate?.toIndexPredicateBuilder(transaction)?.let(this::append)
        }

        if (updateOnConflict) {
            append(" DO UPDATE SET ")

            toBeUpdatedColumns
                .filter { it in values.keys }
                .joinTo(this) {
                    val columnExpr = transaction.identity(it)
                    "$columnExpr=EXCLUDED.$columnExpr"
                }
        } else {
            append(" DO NOTHING ")
        }
    }
}

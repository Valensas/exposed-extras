package com.valensas.data.exposed.util.extension.postgres

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ComparisonOp
import org.jetbrains.exposed.sql.IsNotNullOp
import org.jetbrains.exposed.sql.IsNullOp
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.append

/**
 * For postgres upsert statement usage
 * Converts index_predicate: Op<Boolean> to QueryBuilder
 * Note That only ComparisonOp, IsNullOp and IsNotNullOp are supported
 * The columns need to be converted as their identity properties not fullIdentity because
 * of postgres limitations on index_predicate
 */
fun Op<Boolean>.toIndexPredicateBuilder(transaction: Transaction): QueryBuilder =
    with(QueryBuilder(false)) {
        val indexPredicate = this@toIndexPredicateBuilder
        append(" WHERE ")
        when (indexPredicate) {
            is ComparisonOp -> {
                val expr1 = (indexPredicate.expr1 as? Column<*>)?.let { transaction.identity(it) } ?: indexPredicate.expr1
                val expr2 = (indexPredicate.expr2 as? Column<*>)?.let { transaction.identity(it) } ?: indexPredicate.expr2

                append(expr1)
                append(indexPredicate.opSign)
                append(expr2)
            }
            is IsNullOp -> {
                val expr = (indexPredicate.expr as? Column<*>)?.let { transaction.identity(it) } ?: indexPredicate.expr
                append(expr)
            }
            is IsNotNullOp -> {
                val expr = (indexPredicate.expr as? Column<*>)?.let { transaction.identity(it) } ?: indexPredicate.expr
                append(expr)
            }
            else -> throw IllegalArgumentException("Unsupported type: ${indexPredicate.javaClass} of conflictPredicate")
        }
    }

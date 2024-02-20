package com.valensas.data.exposed.util.extension

import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.springframework.data.domain.Pageable

/**
 * Apply pagination and sorting from a Pageable object to the query.
 *
 * @param page The pageable object to take pagination and sorting parameters from
 * @param table The table to apply the queries
 */
fun Query.pageable(
    page: Pageable,
    table: Table
): Query {
    page.sort.forEach { order ->
        table.columns.firstOrNull { it.name == order.property }?.let {
            val sortOrder = SortOrder.valueOf(order.direction.name)
            val statement = it to sortOrder
            this.orderBy(statement)
        }
    }
    this.limit(page.pageSize, page.offset)
    return this
}

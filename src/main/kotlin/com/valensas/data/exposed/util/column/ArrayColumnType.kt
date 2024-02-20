package com.valensas.data.exposed.util.column

import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection

class ArrayColumnType(
    private val type: ColumnType
) : ColumnType() {
    override fun sqlType() = "${type.sqlType()} ARRAY"

    override fun valueToDB(value: Any?): Any? {
        val columnType = type.sqlType().split("(")[0]
        return when (value) {
            is Array<*> -> (TransactionManager.current().connection.connection as Connection).createArrayOf(columnType, value)
            else -> super.valueToDB(value)
        }
    }

    override fun valueFromDB(value: Any): Any {
        if (value is java.sql.Array) {
            return (value.array as Array<*>).toList().filterNotNull().map(type::valueFromDB).toTypedArray()
        }
        throw IllegalArgumentException("Unable convert $value to array. Expected an instance of java.sql.Array")
    }
}

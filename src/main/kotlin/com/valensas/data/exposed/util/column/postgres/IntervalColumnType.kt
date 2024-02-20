package com.valensas.data.exposed.util.column.postgres

import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.util.PGInterval
import org.postgresql.util.PGobject
import java.time.Duration
import java.util.GregorianCalendar

class IntervalColumnType : ColumnType() {
    override fun sqlType() = "INTERVAL"

    override fun setParameter(
        stmt: PreparedStatementApi,
        index: Int,
        value: Any?
    ) {
        val obj = PGInterval()
        obj.value = (value as Duration).toString()
        obj.type = sqlType()
        stmt[index] = obj
    }

    override fun valueFromDB(value: Any): Any {
        if (value !is PGobject) {
            // We didn't receive a PGobject (the format of stuff actually coming from the DB).
            // In that case "value" should already be an object of type T.
            return value
        }

        if (value !is PGInterval) {
            throw IllegalArgumentException(
                "Unable convert $value to interval. Expected an instance of ${PGInterval::class.java.canonicalName}"
            )
        }

        return GregorianCalendar()
            .apply { timeInMillis = 0 }
            .apply { value.add(this) }
            .let { Duration.ofMillis(it.timeInMillis) }
    }
}

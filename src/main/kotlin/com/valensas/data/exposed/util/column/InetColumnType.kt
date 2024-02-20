package com.valensas.data.exposed.util.column

import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject
import java.net.InetAddress

class InetColumnType : ColumnType() {
    override fun sqlType() = "INET"

    override fun setParameter(
        stmt: PreparedStatementApi,
        index: Int,
        value: Any?
    ) {
        val obj = PGobject()
        obj.type = sqlType()
        obj.value = value?.let { it as InetAddress }?.hostAddress
        stmt[index] = obj
    }

    override fun valueFromDB(value: Any): Any {
        if (value !is PGobject) {
            // We didn't receive a PGobject (the format of stuff actually coming from the DB).
            // In that case "value" should already be an object of type T.
            return value
        }

        return InetAddress.getByName(value.value)
    }
}

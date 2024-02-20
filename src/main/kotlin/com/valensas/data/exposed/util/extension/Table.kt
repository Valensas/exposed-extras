package com.valensas.data.exposed.util.extension

import com.fasterxml.jackson.databind.ObjectMapper
import com.valensas.data.exposed.util.column.ArrayColumnType
import com.valensas.data.exposed.util.column.InetColumnType
import com.valensas.data.exposed.util.column.postgres.IntervalColumnType
import com.valensas.data.exposed.util.column.postgres.PGEnum
import com.valensas.data.exposed.util.mapper.ExposedObjectMapperBuilder
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.jsonb
import java.net.InetAddress
import java.time.Duration

/**
 *  Defines a column of Array type.
 *
 *  @param name The name of the column
 *  @param columnType The type of objects within the array
 */
fun <T> Table.array(
    name: String,
    columnType: ColumnType
): Column<Array<T>> = registerColumn(name, ArrayColumnType(columnType))

/**
 *  Defines a column of type inet.
 *
 *  @param name The name of the column
 */
fun Table.inet(name: String): Column<InetAddress> = registerColumn(name, InetColumnType())

/**
 * Defines a column of type jsonb. Same as org.jetbrains.exposed.sql.json.jsonb but makes it
 * easier to use Jackson ObjectMapper.
 *
 * @param name The name of the column
 * @param objectMapper The ObjectMapper to use serialization and deserialization
 */
inline fun <reified T : Any> Table.jsonb(
    name: String,
    objectMapper: ObjectMapper = ExposedObjectMapperBuilder.build()
): Column<T> =
    jsonb(name, objectMapper::writeValueAsString) {
        objectMapper.readValue(it, T::class.java)
    }

/**
 * Defines a column of enum type. Same as org.jetbrains.exposed.sql.Table.enumerationByName but allows
 * for arbitrary lengths.
 *
 * @param columnName The name of the column
 * @param enumName The name of the user defined enum type
 */
inline fun <reified T : Enum<T>> Table.enum(
    columnName: String,
    enumName: String
): Column<T> =
    customEnumeration(
        columnName,
        enumName,
        { java.lang.Enum.valueOf(T::class.java, it as String) },
        { PGEnum(enumName, it) }
    )

/**
 * Defines a column of interval type.
 *
 * @param name The name of the column
 */
fun Table.interval(name: String): Column<Duration> = registerColumn(name, IntervalColumnType())

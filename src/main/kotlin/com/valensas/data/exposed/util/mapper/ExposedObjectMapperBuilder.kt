package com.valensas.data.exposed.util.mapper

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

object ExposedObjectMapperBuilder {
    fun build(): ObjectMapper {
        val objectMapper: ObjectMapper = jacksonObjectMapper()

        val bigDecimalModule =
            SimpleModule("big-decimal-mappers")
                .addDeserializer(BigDecimal::class.java, BigDecimalMoneyDeserializer(32))

        val instantModule =
            SimpleModule("instant-module")
                .addSerializer(Instant::class.java, InstantSerializer())

        objectMapper.registerModule(instantModule)
        objectMapper.registerModule(bigDecimalModule)

        objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        objectMapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)

        return objectMapper
    }

    class BigDecimalMoneyDeserializer(
        private val scale: Int
    ) : JsonDeserializer<BigDecimal>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(
            jp: JsonParser,
            ctx: DeserializationContext
        ): BigDecimal {
            return jp.decimalValue.setScale(scale, RoundingMode.HALF_UP)
        }
    }

    class InstantSerializer : JsonSerializer<Instant>() {
        override fun serialize(
            value: Instant?,
            gen: JsonGenerator?,
            serializers: SerializerProvider?
        ) {
            gen?.writeString(
                dateTimeFormatter.format(value)
            )
        }

        companion object {
            val dateTimeFormatter: DateTimeFormatter = DateTimeFormatterBuilder().appendInstant(3).toFormatter()
        }
    }
}

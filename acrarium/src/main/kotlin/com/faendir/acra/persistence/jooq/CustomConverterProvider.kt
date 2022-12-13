package com.faendir.acra.persistence.jooq

import org.jooq.Converter
import org.jooq.ConverterProvider
import org.jooq.impl.DefaultConverterProvider
import org.springframework.stereotype.Component

@Component
class CustomConverterProvider(convertersIn: List<Converter<*, *>>) : ConverterProvider {
    private val converters: Map<Class<*>, Map<Class<*>, Converter<*, *>>> =
        (convertersIn + convertersIn.map { it.invert() }).groupBy { it.fromType() }.mapValues { (_, value) -> value.associateBy { it.toType() } }

    private val delegate = DefaultConverterProvider()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?, U : Any?> provide(t: Class<T>, u: Class<U>): Converter<T, U>? {
        return converters[t]?.get(u) as Converter<T, U>? ?: delegate.provide(t, u)
    }
}

fun <T, U> Converter<T, U>.invert(): Converter<U, T> = Converter.of(toType(), fromType(), this::to, this::from)


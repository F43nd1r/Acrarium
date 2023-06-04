/*
 * (C) Copyright 2022 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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


/*
 * (C) Copyright 2020 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.acra.util

import com.faendir.acra.model.Stacktrace
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.UserService
import com.querydsl.core.types.dsl.BooleanExpression
import org.json.JSONObject
import proguard.retrace.ReTrace
import java.io.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

inline fun <reified T : Number> getConverter(): (Number) -> T {
    return when (T::class.java) {
        java.lang.Integer::class.java -> { number -> number.toInt() as T }
        java.lang.Long::class.java -> { number -> number.toLong() as T }
        java.lang.Double::class.java -> { number -> number.toDouble() as T }
        java.lang.Float::class.java -> { number -> number.toFloat() as T }
        java.lang.Character::class.java -> { number -> number.toChar() as T }
        java.lang.Short::class.java -> { number -> number.toShort() as T }
        java.lang.Byte::class.java -> { number -> number.toByte() as T }
        else -> throw IllegalArgumentException("unknown number type ${T::class.java}")
    }
}

fun <T : Any> Optional<T>.toNullable(): T? = this.orElse(null)

fun UserService.getCurrentUser() = getUser(SecurityUtils.getUsername())!!


inline fun <T> Array<out T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? = indexOfFirst(predicate).takeIf { it != -1 }

fun JSONObject.findInt(key: String): Int? = tryOrNull { getInt(key) }

fun JSONObject.findString(key: String): String? = tryOrNull { getString(key) }

fun String.ensureTrailing(suffix: String) = if (endsWith(suffix)) this else this + suffix

private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

fun String.toDate(): ZonedDateTime = try {
    ZonedDateTime.parse(this, formatter)
} catch (e: DateTimeParseException) {
    ZonedDateTime.now()
}

fun Stacktrace.retrace(mappings: String): String {
    try {
        StringReader(mappings).use { mappingsReader ->
            LineNumberReader(StringReader(stacktrace)).use { stacktraceReader ->
                StringWriter().use { output ->
                    ReTrace(ReTrace.STACK_TRACE_EXPRESSION, false, mappingsReader).retrace(stacktraceReader, PrintWriter(output))
                    return output.toString()
                }
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        return stacktrace
    }
}

inline fun <T, R> Iterable<T>.fold(initial: R, crossinline operation: R.(T) -> R): R {
    return fold(initial, { acc: R, t: T -> acc.operation(t) })
}

inline fun <T : Any, R> T.tryOrNull(f: T.() -> R): R? {
    return try {
        f()
    } catch (e: Exception) {
        null
    }
}

inline fun catching(f: () -> Unit) {
    try {
        f()
    } catch (e: Throwable) {
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <T : Any, R> T.equalsBy(other: Any?, id: T.() -> R): Boolean = when {
    this === other -> true
    other == null || javaClass != other.javaClass -> false
    else -> id() == (other as T).id()
}

const val PARAM_APP = "app"
const val PARAM_BUG = "bug"
const val PARAM_INSTALLATION = "installation"
const val PARAM_REPORT = "report"

infix fun BooleanExpression.and(other: BooleanExpression): BooleanExpression = and(other)

fun <T, U, V> Iterable<T>.zip(u: Iterable<U>, v: Iterable<V>): List<Triple<T, U, V>> {
    return zip(u.zip(v)) { t, (u, v) -> Triple(t, u, v) }
}
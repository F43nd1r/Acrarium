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

import org.intellij.lang.annotations.Language
import org.json.JSONObject
import proguard.retrace.ReTrace
import java.io.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

fun <T : Any> Optional<T>.toNullable(): T? = this.orElse(null)

inline fun <T> Array<out T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? = indexOfFirst(predicate).takeIf { it != -1 }

fun JSONObject.findInt(key: String): Int? = tryOrNull { getInt(key) }

fun JSONObject.findString(key: String): String? = tryOrNull { getString(key) }

fun String.ensureTrailing(suffix: String) = if (endsWith(suffix)) this else this + suffix

private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

fun String.toDate(): Instant = try {
    Instant.from(formatter.parse(this))
} catch (e: DateTimeParseException) {
    Instant.now()
}

fun String.retrace(mappings: String): String {
    try {
        StringReader(mappings).use { mappingsReader ->
            LineNumberReader(StringReader(this)).use { stacktraceReader ->
                StringWriter().use { output ->
                    ReTrace(ReTrace.STACK_TRACE_EXPRESSION, false, mappingsReader).retrace(stacktraceReader, PrintWriter(output))
                    return output.toString()
                }
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        return this
    }
}

inline fun <T, R> Iterable<T>.fold(initial: R, crossinline operation: R.(T) -> R): R = fold(initial) { acc: R, t: T -> acc.operation(t) }

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
    } catch (_: Throwable) {
    }
}

fun <T, U, V> Iterable<T>.zip(u: Iterable<U>, v: Iterable<V>): List<Triple<T, U, V>> {
    return zip(u.zip(v)) { t, (u, v) -> Triple(t, u, v) }
}

@Suppress("NOTHING_TO_INLINE")
inline fun sql(
    @Language("SQL")
    sql: String
) = sql

fun Instant.toUtcLocal(): LocalDateTime = LocalDateTime.ofInstant(this, ZoneOffset.UTC)
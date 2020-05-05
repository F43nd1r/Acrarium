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
import org.apache.commons.logging.LogFactory
import org.json.JSONObject
import proguard.retrace.ReTrace
import java.io.IOException
import java.io.LineNumberReader
import java.io.PrintWriter
import java.io.StringReader
import java.io.StringWriter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

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

fun <T : Any> Optional<T>.toNullable(): T? = this.orElse(null);

fun UserService.getCurrentUser() = getUser(SecurityUtils.getUsername())!!


inline fun <T> Array<out T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? = indexOfFirst(predicate).let { if (it == -1) null else it }

fun JSONObject.getIntOrNull(key: String): Int? = try {
    getInt(key)
} catch (e: Exception) {
    null
}

fun String.ensureTrailing(suffix: String) = if (endsWith(suffix)) this else this + suffix

operator fun <T> KProperty0<T>.getValue(thisRef: Any?, property: KProperty<*>) = get()
operator fun <T> KMutableProperty0<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) = set(value)


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
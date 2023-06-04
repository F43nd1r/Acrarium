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
package com.faendir.acra.persistence

import com.faendir.acra.dataprovider.AcrariumSort
import com.faendir.acra.jooq.generated.tables.Report
import com.faendir.acra.persistence.bug.BugIdentifier
import com.vaadin.flow.data.provider.SortDirection
import org.jooq.*

fun <T, R : Record1<T>> ResultQuery<R>.fetchValue(): T? = fetchOne()?.value1()

inline fun <reified T> ResultQuery<*>.fetchValueInto(): T? = fetchOneInto(T::class.java)

fun <T, R : Record1<T>> ResultQuery<R>.fetchList(): List<T> = fetch().map { it.value1() }

inline fun <reified T> ResultQuery<*>.fetchListInto(): List<T> = fetchInto(T::class.java)

fun <T> Field<T>.direction(direction: SortDirection): SortField<T> = if (direction == SortDirection.ASCENDING) asc() else desc()

fun <T : SortDefinition> List<AcrariumSort<T>>.asOrderFields() = map { it.sort.field.direction(it.direction) }

fun Condition.and(filters: Set<FilterDefinition>) = filters.fold(this) { a, b -> a.and(b.condition) }

fun Report.hasBugIdentifier(bugIdentifier: BugIdentifier) =
    APP_ID.eq(bugIdentifier.appId)
        .and(EXCEPTION_CLASS.eq(bugIdentifier.exceptionClass))
        .and(MESSAGE.nullSafeEq(bugIdentifier.message))
        .and(CRASH_LINE.nullSafeEq(bugIdentifier.crashLine))
        .and(CAUSE.nullSafeEq(bugIdentifier.cause))

fun com.faendir.acra.jooq.generated.tables.BugIdentifier.matches(bugIdentifier: BugIdentifier) =
    APP_ID.eq(bugIdentifier.appId)
        .and(EXCEPTION_CLASS.eq(bugIdentifier.exceptionClass))
        .and(MESSAGE.nullSafeEq(bugIdentifier.message))
        .and(CRASH_LINE.nullSafeEq(bugIdentifier.crashLine))
        .and(CAUSE.nullSafeEq(bugIdentifier.cause))

fun <T> Field<T>.nullSafeEq(value: T?) = if (value != null) eq(value) else isNull

@Suppress("UNCHECKED_CAST")
val <T : Any> Field<T?>.NOT_NULL: Field<T> get() = this as Field<T>

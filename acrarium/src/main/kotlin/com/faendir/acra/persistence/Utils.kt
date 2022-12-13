package com.faendir.acra.persistence

import com.faendir.acra.dataprovider.AcrariumSort
import com.faendir.acra.jooq.generated.tables.Report
import com.faendir.acra.persistence.bug.BugIdentifier
import com.vaadin.flow.data.provider.SortDirection
import org.jooq.Condition
import org.jooq.Field
import org.jooq.Record1
import org.jooq.ResultQuery
import org.jooq.SortField

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

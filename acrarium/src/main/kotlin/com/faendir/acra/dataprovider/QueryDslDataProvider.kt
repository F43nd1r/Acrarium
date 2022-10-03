/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.dataprovider

import com.faendir.acra.util.fold
import com.faendir.acra.util.toNullable
import com.querydsl.core.types.Expression
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQuery
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.SortDirection
import java.util.stream.Stream

/**
 * @author lukas
 * @since 30.05.18
 */
class QueryDslDataProvider<T : Any>(private val fetchProvider: () -> JPAQuery<T>, private val countProvider: () -> JPAQuery<*>) :
    AcrariumDataProvider<T, BooleanExpression, Expression<out Comparable<*>>>() {
    constructor(base: JPAQuery<T>) : this(base, base)
    constructor(fetchBase: JPAQuery<T>, countBase: JPAQuery<*>) : this(fetchBase::clone, countBase::clone)

    override fun fetch(filters: Set<BooleanExpression>, sort: List<AcrariumSort<Expression<out Comparable<*>>>>, offset: Int, limit: Int): Stream<T> =
        fetchProvider.invoke()
            .where(*filters.toTypedArray())
            .offset(offset.toLong())
            .limit(limit.toLong())
            .orderBy(*sort.map { it.toSpecifier() }.toTypedArray())
            .fetch()
            .stream()

    override fun size(filters: Set<BooleanExpression>): Int = Math.toIntExact(countProvider.invoke().where(*filters.toTypedArray()).fetchCount())

    private fun AcrariumSort<Expression<out Comparable<*>>>.toSpecifier() = OrderSpecifier(if (direction == SortDirection.ASCENDING) Order.ASC else Order.DESC, sort)
}


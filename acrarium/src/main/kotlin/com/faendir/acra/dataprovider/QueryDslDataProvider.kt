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
import com.querydsl.jpa.impl.JPAQuery
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.Query
import java.util.stream.Stream

/**
 * @author lukas
 * @since 30.05.18
 */
class QueryDslDataProvider<T>(private val fetchProvider: () -> JPAQuery<T>, private val countProvider: () -> JPAQuery<*>) : AbstractBackEndDataProvider<T, QueryDslFilter>() {
    constructor(base: JPAQuery<T>) : this(base, base)
    constructor(fetchBase: JPAQuery<T>, countBase: JPAQuery<*>) : this(fetchBase::clone, countBase::clone)

    override fun fetchFromBackEnd(query: Query<T, QueryDslFilter>): Stream<T> {
        return fetchProvider.invoke().also { query.filter.toNullable()?.apply(it) }.offset(query.offset.toLong()).limit(query.offset.toLong() + query.limit.toLong())
            .also { q -> query.sortOrders?.filterIsInstance<QueryDslSortOrder>()?.fold(q) { orderBy(it.toSpecifier()) } }.fetch().stream()
    }

    override fun sizeInBackEnd(query: Query<T, QueryDslFilter>): Int {
        return Math.toIntExact(countProvider.invoke().also { query.filter.toNullable()?.apply(it) }.fetchCount())
    }
}


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
package com.faendir.acra.dataprovider

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.QuerySortOrder
import com.vaadin.flow.data.provider.SortDirection
import java.util.stream.Stream

abstract class AcrariumDataProvider<T : Any, F : Any, S : Any> : AbstractBackEndDataProvider<T, () -> Set<F>>() {
    override fun fetchFromBackEnd(query: Query<T, () -> Set<F>>): Stream<T> =
        fetch(query.filters(), query.sortOrders?.filterIsInstance<AcrariumSort<S>>().orEmpty(), query.offset, query.limit)

    abstract fun fetch(filters: Set<F>, sort: List<AcrariumSort<S>>, offset: Int, limit: Int): Stream<T>

    override fun sizeInBackEnd(query: Query<T, () -> Set<F>>): Int = size(query.filters())

    abstract fun size(filters: Set<F>): Int

    private fun Query<T, () -> Set<F>>.filters(): Set<F> = filter.map { it.invoke() }.orElse(emptySet())
}

class AcrariumSort<S>(val sort: S, direction: SortDirection) : QuerySortOrder(sort.toString(), direction)
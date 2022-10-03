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
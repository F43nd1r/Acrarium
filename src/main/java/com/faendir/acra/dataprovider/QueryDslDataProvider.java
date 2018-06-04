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

package com.faendir.acra.dataprovider;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.shared.data.sort.SortDirection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author lukas
 * @since 30.05.18
 */
public class QueryDslDataProvider<T> extends AbstractBackEndDataProvider<T, Void> {
    private final List<SizeListener> sizeListeners;
    private final Supplier<JPAQuery<T>> fetchProvider;
    private final Supplier<JPAQuery<?>> countProvider;
    private final Map<String, Expression<? extends Comparable>> sortOptions;

    public QueryDslDataProvider(JPAQuery<T> base) {
        this(base, base);
    }

    public QueryDslDataProvider(JPAQuery<T> fetchBase, JPAQuery<?> countBase) {
        this(fetchBase::clone, countBase::clone);
    }

    public QueryDslDataProvider(Supplier<JPAQuery<T>> fetchProvider, Supplier<JPAQuery<?>> countProvider){
        this.fetchProvider = fetchProvider;
        this.countProvider = countProvider;
        sizeListeners = new ArrayList<>();
        sortOptions = new HashMap<>();
    }

    public void addSizeListener(SizeListener listener) {
        sizeListeners.add(listener);
    }

    public String addSortable(Expression<? extends Comparable> expression) {
        String id = String.valueOf(sortOptions.size());
        sortOptions.put(id, expression);
        return id;
    }

    @Override
    protected Stream<T> fetchFromBackEnd(Query<T, Void> query) {
        JPAQuery<T> q = fetchProvider.get().offset(query.getOffset()).limit(query.getLimit());
        for (QuerySortOrder order : query.getSortOrders()) {
            Expression<? extends Comparable> sort = sortOptions.get(order.getSorted());
            if (sort != null) {
                q = q.orderBy(new OrderSpecifier<>(order.getDirection() == SortDirection.ASCENDING ? Order.ASC : Order.DESC, sort));
            }
        }
        return q.fetch().stream();
    }

    @Override
    protected int sizeInBackEnd(Query<T, Void> query) {
        int result = Math.toIntExact(countProvider.get().fetchCount());
        sizeListeners.forEach(listener -> listener.sizeChanged(result));
        return result;
    }

    @FunctionalInterface
    public interface SizeListener {
        void sizeChanged(int size);
    }
}

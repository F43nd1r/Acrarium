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
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import org.springframework.lang.NonNull;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author lukas
 * @since 30.05.18
 */
public class QueryDslDataProvider<T> extends AbstractBackEndDataProvider<T, Void> {
    private final Supplier<JPAQuery<T>> fetchProvider;
    private final Supplier<JPAQuery<?>> countProvider;

    public QueryDslDataProvider(JPAQuery<T> base) {
        this(base, base);
    }

    public QueryDslDataProvider(JPAQuery<T> fetchBase, JPAQuery<?> countBase) {
        this(fetchBase::clone, countBase::clone);
    }

    public QueryDslDataProvider(Supplier<JPAQuery<T>> fetchProvider, Supplier<JPAQuery<?>> countProvider){
        this.fetchProvider = fetchProvider;
        this.countProvider = countProvider;
    }

    @Override
    protected Stream<T> fetchFromBackEnd(Query<T, Void> query) {
        JPAQuery<T> q = fetchProvider.get().offset(query.getOffset()).limit(query.getLimit());
        for (QuerySortOrder order : query.getSortOrders()) {
            if(order instanceof QueryDslSortOrder) {
                q = q.orderBy(((QueryDslSortOrder) order).toSpecifier());
            }
        }
        return q.fetch().stream();
    }

    @Override
    protected int sizeInBackEnd(Query<T, Void> query) {
        return Math.toIntExact(countProvider.get().fetchCount());
    }

    public static class QueryDslSortOrder extends QuerySortOrder {
        private final Expression<? extends Comparable> expression;

        public QueryDslSortOrder(@NonNull Expression<? extends Comparable> expression, @NonNull SortDirection direction) {
            super(expression.toString(), direction);
            this.expression = expression;
        }

        public OrderSpecifier<?> toSpecifier() {
            return new OrderSpecifier<>(getDirection() == SortDirection.ASCENDING ? Order.ASC : Order.DESC, expression);
        }
    }
}

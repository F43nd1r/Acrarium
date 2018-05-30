package com.faendir.acra.dataprovider;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.shared.data.sort.SortDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author lukas
 * @since 30.05.18
 */
public class QueryDslDataProvider<T> extends AbstractBackEndDataProvider<T, Void> implements ObservableDataProvider<T, Void> {
    private final List<SizeListener> sizeListeners;
    private final JPAQuery<T> fetchBase;
    private final JPAQuery<?> countBase;

    public QueryDslDataProvider(JPAQuery<T> base){
        this(base, base);
    }

    public QueryDslDataProvider(JPAQuery<T> fetchBase, JPAQuery<?> countBase) {
        this.fetchBase = fetchBase;
        this.countBase = countBase;
        sizeListeners = new ArrayList<>();
    }

    @Override
    public void addSizeListener(SizeListener listener) {
        sizeListeners.add(listener);
    }

    @Override
    public void removeSizeListener(SizeListener listener) {
        sizeListeners.remove(listener);
    }

    @Override
    protected Stream<T> fetchFromBackEnd(Query<T, Void> query) {
        JPAQuery<T> q = fetchBase.clone().offset(query.getOffset()).limit(query.getLimit());
        for (QuerySortOrder order : query.getSortOrders()){
            q = q.orderBy(new OrderSpecifier<>(order.getDirection() == SortDirection.ASCENDING ? Order.ASC : Order.DESC, Expressions.asComparable(order.getSorted())));
        }
        return q.fetch().stream();
    }

    @Override
    protected int sizeInBackEnd(Query<T, Void> query) {
        int result = Math.toIntExact(countBase.fetchCount());
        sizeListeners.forEach(listener -> listener.sizeChanged(result));
        return result;
    }
}

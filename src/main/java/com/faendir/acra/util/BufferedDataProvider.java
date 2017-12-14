package com.faendir.acra.util;

import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.Query;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lukas
 * @since 13.12.2017
 */
public class BufferedDataProvider<P, T> extends AbstractBackEndDataProvider<T, Void> {
    private static final int PAGE_SIZE = 32;
    private final P p;
    private final BiFunction<P, Pageable, Slice<T>> getter;
    private final Function<P, Integer> counter;

    public BufferedDataProvider(P p, BiFunction<P, Pageable, Slice<T>> getter, Function<P, Integer> counter) {
        this.p = p;
        this.getter = getter;
        this.counter = counter;
    }

    @Override
    protected Stream<T> fetchFromBackEnd(Query<T, Void> query) {
        Sort sort = Sort.by(query.getSortOrders().stream().map(OrderAdapter::new).collect(Collectors.toList()));
        Slice<T> slice = getter.apply(p, PageRequest.of(query.getOffset() / PAGE_SIZE, PAGE_SIZE, sort));
        if (!slice.hasContent()) return Stream.empty();
        List<T> content = slice.getContent();
        int ignore = query.getOffset() % PAGE_SIZE;
        int size = content.size() - ignore;
        Stream<T> result = content.stream().skip(ignore);
        while (size < query.getLimit() && slice.hasNext()) {
            slice = getter.apply(p, slice.nextPageable());
            if (slice.hasContent()) {
                content = slice.getContent();
                size += content.size();
                result = Stream.concat(result, content.stream());
            }
        }
        return result;
    }

    @Override
    protected int sizeInBackEnd(Query<T, Void> query) {
        return counter.apply(p);
    }
}

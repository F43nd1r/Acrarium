package com.faendir.acra.util;

import com.faendir.acra.config.AcraConfiguration;
import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lukas
 * @since 13.12.2017
 */
public class BufferedDataProvider<T> extends AbstractBackEndDataProvider<T, Void> {
    private final int pageSize;
    private final Function<Pageable, Slice<T>> getter;
    private final IntSupplier counter;

    private BufferedDataProvider(int pageSize, Function<Pageable, Slice<T>> getter, IntSupplier counter) {
        this.getter = getter;
        this.counter = counter;
        this.pageSize = pageSize;
    }

    @Override
    protected Stream<T> fetchFromBackEnd(Query<T, Void> query) {
        Sort sort = Sort.by(query.getSortOrders().stream().map(OrderAdapter::new).collect(Collectors.toList()));
        Slice<T> slice = getter.apply(PageRequest.of(query.getOffset() / pageSize, pageSize, sort));
        if (!slice.hasContent()) return Stream.empty();
        List<T> content = slice.getContent();
        int ignore = query.getOffset() % pageSize;
        int size = content.size() - ignore;
        Stream<T> result = content.stream().skip(ignore);
        while (size < query.getLimit() && slice.hasNext()) {
            slice = getter.apply(slice.nextPageable());
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
        return counter.getAsInt();
    }

    @Component
    public static class Factory {
        private final AcraConfiguration configuration;

        @Autowired
        public Factory(AcraConfiguration configuration) {
            this.configuration = configuration;
        }

        public <P, T> BufferedDataProvider<T> create(P parameter, BiFunction<P, Pageable, Slice<T>> getter, Function<P, Integer> counter) {
            return new BufferedDataProvider<>(configuration.getPaginationSize(), pageable -> getter.apply(parameter, pageable), () -> counter.apply(parameter));
        }
    }
}

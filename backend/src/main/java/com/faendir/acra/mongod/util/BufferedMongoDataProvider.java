package com.faendir.acra.mongod.util;

import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.shared.data.sort.SortDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lukas
 * @since 07.12.2017
 */
public class BufferedMongoDataProvider<T,R> extends AbstractBackEndDataProvider<R, Void> {
    private static final int PAGE_SIZE = 32;
    private final Function<Pageable, Page<T>> getter;
    private final Function<T, R> transformer;

    public static <T, R> BufferedMongoDataProvider<T, R> of(Function<Pageable, Page<T>> getter, Function<T, R> transformer) {
        return new BufferedMongoDataProvider<>(getter, transformer);
    }

    public static <R> BufferedMongoDataProvider<R,R> of(Function<Pageable, Page<R>> getter){
        return of(getter, Function.identity());
    }

    private BufferedMongoDataProvider(Function<Pageable, Page<T>> getter, Function<T, R> transformer){
        this.getter = getter;
        this.transformer = transformer;
    }

    @Override
    protected Stream<R> fetchFromBackEnd(Query<R, Void> query) {
        Sort sort = Sort.by(query.getSortOrders().stream()
                .map(s -> new Sort.Order(s.getDirection() == SortDirection.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC, s.getSorted()))
                .collect(Collectors.toList()));
        Page<T> page = getter.apply(PageRequest.of(query.getOffset() / PAGE_SIZE, PAGE_SIZE, sort));
        if (!page.hasContent()) return Stream.empty();
        List<T> content = page.getContent();
        int ignore = query.getOffset() % PAGE_SIZE;
        int size = content.size() - ignore;
        Stream<T> result = content.stream().skip(ignore);
        while (size < query.getLimit() && page.hasNext()) {
            page = getter.apply(page.nextPageable());
            if (page.hasContent()) {
                content = page.getContent();
                size += content.size();
                result = Stream.concat(result, content.stream());
            }
        }
        return result.map(transformer);
    }

    @Override
    protected int sizeInBackEnd(Query<R, Void> query) {
        return (int) getter.apply(PageRequest.of(0, 1)).getTotalElements();
    }
}

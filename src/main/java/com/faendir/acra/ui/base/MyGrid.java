package com.faendir.acra.ui.base;

import com.faendir.acra.dataprovider.QueryDslDataProvider;
import com.querydsl.core.types.Expression;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;
import org.springframework.lang.NonNull;

import java.util.Set;

/**
 * @author lukas
 * @since 13.07.18
 */
public class MyGrid<T> extends Composite<Grid<T>> {
    private final QueryDslDataProvider<T> dataProvider;
    public MyGrid(QueryDslDataProvider<T> dataProvider) {
        this.dataProvider = dataProvider;
        getContent().setDataProvider(dataProvider);
        getContent().setSizeFull();
    }

    @NonNull
    public Grid.Column<T> addColumn(@NonNull ValueProvider<T, ?> valueProvider, @NonNull String caption) {
        return getContent().addColumn(valueProvider).setHeader(caption);
    }

    @NonNull
    public Grid.Column<T> addColumn(@NonNull ValueProvider<T, ?> valueProvider, @NonNull Expression<? extends Comparable> sort, @NonNull String caption) {
        Grid.Column<T> column = addColumn(valueProvider, caption);
        column.setId(dataProvider.addSortable(sort));
        return column;
    }

    @NonNull
    public Grid.Column<T> addColumn(@NonNull Renderer<T> renderer, @NonNull String caption) {
        return getContent().addColumn(renderer).setHeader(caption);
    }

    @NonNull
    public Grid.Column<T> addColumn(@NonNull Renderer<T> renderer, @NonNull Expression<? extends Comparable> sort, @NonNull String caption) {
        Grid.Column<T> column = addColumn(renderer, caption);
        column.setId(dataProvider.addSortable(sort));
        return column;
    }



    public Registration addSelectionListener(SelectionListener<Grid<T>, T> listener) {
        return getContent().addSelectionListener(listener);
    }

    public void setSelectionMode(Grid.SelectionMode selectionMode) {
        getContent().setSelectionMode(selectionMode);
    }

    public void deselectAll() {
        getContent().deselectAll();
    }

    public QueryDslDataProvider<T> getDataProvider() {
        return dataProvider;
    }

    public Set<T> getSelectedItems() {
        return getContent().getSelectedItems();
    }
}

package com.faendir.acra.ui.base;

import com.faendir.acra.dataprovider.QueryDslDataProvider;
import com.querydsl.core.types.Expression;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.shared.Registration;
import org.springframework.lang.NonNull;

import java.util.Set;
import java.util.function.Function;

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
        getContent().setMultiSort(true);
        getContent().setColumnReorderingAllowed(true);
    }

    @NonNull
    public Grid.Column<T> addColumn(@NonNull ValueProvider<T, ?> valueProvider, @NonNull String caption) {
        return setupColumn(getContent().addColumn(valueProvider), caption);
    }

    @NonNull
    public Grid.Column<T> addColumn(@NonNull ValueProvider<T, ?> valueProvider, @NonNull Expression<? extends Comparable> sort, @NonNull String caption) {
        return setupSortableColumn(addColumn(valueProvider, caption), sort);
    }

    @NonNull
    public Grid.Column<T> addColumn(@NonNull Renderer<T> renderer, @NonNull String caption) {
        return setupColumn(getContent().addColumn(renderer), caption);
    }

    @NonNull
    public Grid.Column<T> addColumn(@NonNull Renderer<T> renderer, @NonNull Expression<? extends Comparable> sort, @NonNull String caption) {
        return setupSortableColumn(addColumn(renderer, caption), sort);
    }

    private Grid.Column<T> setupColumn(@NonNull Grid.Column<T> column, @NonNull String caption) {
        column = column.setHeader(caption).setResizable(true).setFlexGrow(0).setWidth(Math.max(50, caption.length() * 10 + 20) + "px");
        return column;
    }

    private Grid.Column<T> setupSortableColumn(@NonNull Grid.Column<T> column, @NonNull Expression<? extends Comparable> sort) {
        column.setId(dataProvider.addSortable(sort));
        column.setSortable(true);
        return column;
    }

    public Registration addItemClickListener(ComponentEventListener<ItemClickEvent<T>> listener) {
        return getContent().addItemClickListener(listener);
    }

    public <C extends Component & HasUrlParameter<R>, R> void addOnClickNavigation(Class<C> target, Function<T, R> parameterTransformer) {
        getContent().addItemClickListener(e -> getUI().ifPresent(e.getButton() == 1 ? (ui -> ui.getPage().executeJavaScript("window.open(\"" + ui.getRouter().getUrl(target, parameterTransformer.apply(e.getItem())) + "\", \"blank\", \"\");")) : (ui -> ui.navigate(target, parameterTransformer.apply(e.getItem())))));
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

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
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.shared.Registration;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * @author lukas
 * @since 13.07.18
 */
public class MyGrid<T> extends Composite<Grid<T>> implements LocaleChangeObserver {
    private final QueryDslDataProvider<T> dataProvider;
    private final Map<Grid.Column<T>, Pair<String, Object[]>> columnCaptions;

    public MyGrid(QueryDslDataProvider<T> dataProvider) {
        this.dataProvider = dataProvider;
        getContent().setDataProvider(dataProvider);
        getContent().setSizeFull();
        getContent().setMultiSort(true);
        getContent().setColumnReorderingAllowed(true);
        columnCaptions = new HashMap<>();
    }

    @NonNull
    public Grid.Column<T> addColumn(@NonNull ValueProvider<T, ?> valueProvider, @NonNull String captionId, Object... params) {
        return setupColumn(getContent().addColumn(valueProvider), captionId, params);
    }

    @NonNull
    public Grid.Column<T> addColumn(@NonNull ValueProvider<T, ?> valueProvider, @NonNull Expression<? extends Comparable> sort, @NonNull String captionId, Object... params) {
        return setupSortableColumn(addColumn(valueProvider, captionId, params), sort);
    }

    @NonNull
    public Grid.Column<T> addColumn(@NonNull Renderer<T> renderer) {
        return getContent().addColumn(renderer);
    }

    @NonNull
    public Grid.Column<T> addColumn(@NonNull Renderer<T> renderer, @NonNull String captionId, Object... params) {
        return setupColumn(getContent().addColumn(renderer), captionId, params);
    }

    @NonNull
    public Grid.Column<T> addColumn(@NonNull Renderer<T> renderer, @NonNull Expression<? extends Comparable> sort, @NonNull String captionId, Object... params) {
        return setupSortableColumn(addColumn(renderer, captionId, params), sort);
    }

    private Grid.Column<T> setupColumn(@NonNull Grid.Column<T> column, @NonNull String captionId, Object... params) {
        String caption = getTranslation(captionId, params);
        column = column.setHeader(caption).setResizable(true).setFlexGrow(0).setWidth(Math.max(50, caption.length() * 10 + 20) + "px");
        columnCaptions.put(column, Pair.of(captionId, params));
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

    public void setHeightToRows() {
        getContent().setHeightByRows(true);
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        columnCaptions.forEach((column, caption) -> column.setHeader(getTranslation(caption.getFirst(), caption.getSecond())));
    }
}

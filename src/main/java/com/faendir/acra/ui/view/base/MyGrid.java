package com.faendir.acra.ui.view.base;

import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.AbstractRenderer;
import com.vaadin.ui.renderers.TextRenderer;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;

/**
 * @author Lukas
 * @since 14.05.2017
 */
public class MyGrid<T> extends Grid<T> {
    public <F> MyGrid(String caption, DataProvider<T, F> dataProvider) {
        super(caption, dataProvider);
        setSizeFull();
    }

    public MyGrid(@Nullable String caption, @NonNull Collection<T> items) {
        super(caption, items);
        setHeightByRows(items.size());
    }

    @NonNull
    public <R> Grid.Column<T, R> addColumn(@NonNull ValueProvider<T, R> valueProvider, @NonNull String caption) {
        return addColumn(valueProvider, new TextRenderer(), caption);
    }

    @NonNull
    public <R> Grid.Column<T, R> addColumn(@NonNull ValueProvider<T, R> valueProvider, @NonNull AbstractRenderer<? super T, ? super R> renderer, @NonNull String caption) {
        return addColumn(valueProvider, renderer).setId(caption).setCaption(caption).setSortable(false);
    }

    @NonNull
    public <R> Grid.Column<T, R> addColumn(@NonNull ValueProvider<T, R> valueProvider, @NonNull String id, @NonNull String caption) {
        return addColumn(valueProvider, new TextRenderer(), id, caption);
    }

    @NonNull
    public <R> Grid.Column<T, R> addColumn(@NonNull ValueProvider<T, R> valueProvider, @NonNull AbstractRenderer<? super T, ? super R> renderer, @NonNull String id,
            @NonNull String caption) {
        return addColumn(valueProvider, renderer).setId(id).setCaption(caption);
    }

    @Override
    public void setItems(@NonNull Collection<T> items) {
        super.setItems(items);
        setHeightByRows(items.size());
    }

    @Override
    public void setHeightByRows(double rows) {
        super.setHeightByRows(rows >= 1 ? rows : 1);
    }
}

package com.faendir.acra.ui.view.base;

import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.AbstractRenderer;
import com.vaadin.ui.renderers.TextRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public MyGrid(@Nullable String caption, @NotNull Collection<T> items) {
        super(caption, items);
        setHeightByRows(items.size());
    }

    @NotNull
    public <R> Grid.Column<T, R> addColumn(@NotNull ValueProvider<T, R> valueProvider, @NotNull String caption) {
        return addColumn(valueProvider, new TextRenderer(), caption);
    }

    @NotNull
    public <R> Grid.Column<T, R> addColumn(@NotNull ValueProvider<T, R> valueProvider, @NotNull AbstractRenderer<? super T, ? super R> renderer, @NotNull String caption) {
        return addColumn(valueProvider, renderer).setId(caption).setCaption(caption).setSortable(false);
    }

    @NotNull
    public <R> Grid.Column<T, R> addColumn(@NotNull ValueProvider<T, R> valueProvider, @NotNull String id, @NotNull String caption) {
        return addColumn(valueProvider, new TextRenderer(), id, caption);
    }

    @NotNull
    public <R> Grid.Column<T, R> addColumn(@NotNull ValueProvider<T, R> valueProvider, @NotNull AbstractRenderer<? super T, ? super R> renderer, @NotNull String id,
                                           @NotNull String caption) {
        return addColumn(valueProvider, renderer).setId(id).setCaption(caption);
    }

    @Override
    public void setItems(@NotNull Collection<T> items) {
        super.setItems(items);
        setHeightByRows(items.size());
    }

    @Override
    public void setHeightByRows(double rows) {
        super.setHeightByRows(rows >= 1 ? rows : 1);
    }
}

package com.faendir.acra.ui.view.base;

import com.vaadin.data.ValueProvider;
import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.AbstractRenderer;
import com.vaadin.ui.renderers.TextRenderer;

import java.util.Collection;

/**
 * @author Lukas
 * @since 14.05.2017
 */
public class MyGrid<T> extends Grid<T> {
    public MyGrid(String caption) {
        super(caption);
    }

    public MyGrid(String caption, Collection<T> items) {
        super(caption, items);
    }

    public <R> Grid.Column<T, R> addColumn(ValueProvider<T, R> valueProvider, String caption) {
        return addColumn(valueProvider, new TextRenderer(), caption);
    }

    public <R> Grid.Column<T, R> addColumn(ValueProvider<T, R> valueProvider, AbstractRenderer<? super T, ? super R> renderer, String caption) {
        return addColumn(valueProvider, renderer).setId(caption).setCaption(caption);
    }
}

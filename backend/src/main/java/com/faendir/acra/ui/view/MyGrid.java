package com.faendir.acra.ui.view;

import com.vaadin.data.ValueProvider;
import com.vaadin.ui.Grid;

import java.util.Collection;

/**
 * @author Lukas
 * @since 14.05.2017
 */
public class MyGrid<T> extends Grid<T> {
    public MyGrid(String caption, Collection<T> items) {
        super(caption, items);
    }

    public Grid.Column addColumn(ValueProvider<T, String> valueProvider, String caption) {
        Grid.Column column = addColumn(valueProvider);
        column.setId(caption);
        column.setCaption(caption);
        return column;
    }
}

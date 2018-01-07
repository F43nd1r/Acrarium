package com.faendir.acra.ui.view.base;

import com.faendir.acra.dataprovider.ObservableDataProvider;
import com.faendir.acra.ui.NavigationManager;
import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Grid;
import com.vaadin.ui.components.grid.ItemClickListener;
import com.vaadin.ui.renderers.AbstractRenderer;
import com.vaadin.ui.renderers.TextRenderer;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.function.Function;

/**
 * @author Lukas
 * @since 14.05.2017
 */
public class MyGrid<T> extends Grid<T> {
    public <F> MyGrid(String caption, ObservableDataProvider<T, F> dataProvider) {
        super(caption, dataProvider);
        setSizeFull();
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

    public void setSizeToRows() {
        ((ObservableDataProvider) getDataProvider()).addSizeListener(this::setHeightByRows);
    }

    public void addOnClickNavigation(@NonNull NavigationManager navigationManager, Class<? extends NamedView> namedView, Function<ItemClick<T>, String> parameterGetter) {
        addItemClickListener(e -> {
            boolean newTab = e.getMouseEventDetails().getButton() == MouseEventDetails.MouseButton.MIDDLE || e.getMouseEventDetails().isCtrlKey();
            navigationManager.navigateTo(namedView, parameterGetter.apply(e), newTab);
        });
    }
}

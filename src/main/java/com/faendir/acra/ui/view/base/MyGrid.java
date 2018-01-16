package com.faendir.acra.ui.view.base;

import com.faendir.acra.client.mygrid.MiddleClickGridExtensionConnector;
import com.faendir.acra.dataprovider.ObservableDataProvider;
import com.faendir.acra.ui.NavigationManager;
import com.vaadin.data.ValueProvider;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.AbstractRenderer;
import com.vaadin.ui.renderers.TextRenderer;
import elemental.json.JsonObject;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.function.Function;

/**
 * @author Lukas
 * @since 14.05.2017
 */
public class MyGrid<T> extends Grid<T> {
    public MyGrid(String caption, ObservableDataProvider<T, ?> dataProvider) {
        super(caption, dataProvider);
        setSizeFull();
        MiddleClickExtension.extend(this);
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

    public static class MiddleClickExtension<T> extends AbstractGridExtension<T> {
        private MiddleClickExtension(MyGrid<T> grid) {
            super.extend(grid);
            grid.registerRpc((rowKey, columnInternalId, details) -> grid.fireEvent(
                    new ItemClick<>(grid, grid.getColumnByInternalId(columnInternalId), grid.getDataCommunicator().getKeyMapper().get(rowKey), details)),
                    MiddleClickGridExtensionConnector.Rpc.class);
        }

        public static void extend(MyGrid<?> grid) {
            new MiddleClickExtension<>(grid);
        }

        @Override
        public void generateData(Object item, JsonObject jsonObject) {
        }

        @Override
        public void destroyData(Object item) {
        }

        @Override
        public void destroyAllData() {
        }

        @Override
        public void refreshData(Object item) {
        }
    }
}

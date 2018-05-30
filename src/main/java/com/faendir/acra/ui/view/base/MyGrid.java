package com.faendir.acra.ui.view.base;

import com.faendir.acra.client.mygrid.MiddleClickGridExtensionConnector;
import com.faendir.acra.dataprovider.QueryDslDataProvider;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.querydsl.core.types.Expression;
import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Composite;
import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.AbstractRenderer;
import com.vaadin.ui.renderers.TextRenderer;
import elemental.json.JsonObject;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Lukas
 * @since 14.05.2017
 */
public class MyGrid<T> extends Composite {
    private final ExposingGrid<T> grid;
    private QueryDslDataProvider<T> dataProvider;

    public MyGrid(String caption, QueryDslDataProvider<T> dataProvider) {
        this.dataProvider = dataProvider;
        grid = new ExposingGrid<>(caption, dataProvider);
        setCompositionRoot(grid);
        setSizeFull();
        MiddleClickExtension.extend(this);
    }

    @NonNull
    public <R> Grid.Column<T, R> addColumn(@NonNull ValueProvider<T, R> valueProvider, @NonNull String caption) {
        return addColumn(valueProvider, new TextRenderer(), caption);
    }

    @NonNull
    public <R> Grid.Column<T, R> addColumn(@NonNull ValueProvider<T, R> valueProvider, @NonNull AbstractRenderer<? super T, ? super R> renderer, @NonNull String caption) {
        return grid.addColumn(valueProvider, renderer).setCaption(caption).setSortable(false);
    }

    @NonNull
    public <R> Grid.Column<T, R> addColumn(@NonNull ValueProvider<T, R> valueProvider, @NonNull Expression<? extends Comparable> sort, @NonNull String caption) {
        return addColumn(valueProvider, new TextRenderer(), sort, caption);
    }

    @NonNull
    public <R> Grid.Column<T, R> addColumn(@NonNull ValueProvider<T, R> valueProvider, @NonNull AbstractRenderer<? super T, ? super R> renderer,
            @NonNull Expression<? extends Comparable> sort, @NonNull String caption) {
        return grid.addColumn(valueProvider, renderer).setId(dataProvider.addSortable(sort)).setCaption(caption);
    }

    @NonNull
    public <R> Grid.Column<T, R> addColumn(@NonNull ValueProvider<T, R> valueProvider, @NonNull AbstractRenderer<? super T, ? super R> renderer) {
        return grid.addColumn(valueProvider, renderer).setSortable(false);
    }

    public void setItems(@NonNull Collection<T> items) {
        grid.setItems(items);
        setHeightByRows(items.size());
    }

    public void setHeightByRows(double rows) {
        grid.setHeightByRows(rows >= 1 ? rows : 1);
    }

    public void setSizeToRows() {
        dataProvider.addSizeListener(rows -> getUI().access(() -> setHeightByRows(rows)));
    }

    public void setBodyRowHeight(double rowHeight) {
        grid.setBodyRowHeight(rowHeight);
    }

    public void setSelectionMode(Grid.SelectionMode selectionMode) {
        grid.setSelectionMode(selectionMode);
    }

    public void sort(Grid.Column<T, ?> column, SortDirection direction) {
        grid.sort(column, direction);
    }

    public Set<T> getSelectedItems() {
        return grid.getSelectedItems();
    }

    public void select(T item) {
        grid.select(item);
    }

    public void addOnClickNavigation(@NonNull NavigationManager navigationManager, Class<? extends BaseView> namedView,
            Function<Grid.ItemClick<T>, String> parameterGetter) {
        grid.addItemClickListener(e -> {
            boolean newTab = e.getMouseEventDetails().getButton() == MouseEventDetails.MouseButton.MIDDLE || e.getMouseEventDetails().isCtrlKey();
            navigationManager.navigateTo(namedView, parameterGetter.apply(e), newTab);
        });
    }

    public QueryDslDataProvider<T> getDataProvider() {
        return dataProvider;
    }

    public void setDataProvider(QueryDslDataProvider<T> dataProvider) {
        this.dataProvider = dataProvider;
        grid.setDataProvider(dataProvider);
    }

    public static class MiddleClickExtension<T> extends Grid.AbstractGridExtension<T> {
        private MiddleClickExtension(MyGrid<T> myGrid) {
            ExposingGrid<T> grid = myGrid.grid;
            super.extend(grid);
            registerRpc((rowIndex, rowKey, columnInternalId, details) -> myGrid.fireEvent(new Grid.ItemClick<>(grid,
                    grid.getColumnByInternalId(columnInternalId),
                    grid.getDataCommunicator().getKeyMapper().get(rowKey),
                    details,
                    rowIndex)), MiddleClickGridExtensionConnector.Rpc.class);
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

    private static class ExposingGrid<T> extends Grid<T> {
        ExposingGrid(String caption, DataProvider<T, ?> dataProvider) {
            super(caption, dataProvider);
        }

        @Override
        public Column<T, ?> getColumnByInternalId(String columnId) {
            return super.getColumnByInternalId(columnId);
        }
    }
}

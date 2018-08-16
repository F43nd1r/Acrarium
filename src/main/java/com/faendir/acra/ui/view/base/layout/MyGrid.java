/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.ui.view.base.layout;

import com.faendir.acra.client.mygrid.GridMiddleClickExtensionConnector;
import com.faendir.acra.dataprovider.QueryDslDataProvider;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.ui.view.base.navigation.BaseView;
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
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;
import org.vaadin.spring.i18n.I18N;
import org.vaadin.spring.i18n.support.Translatable;

import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Lukas
 * @since 14.05.2017
 */
public class MyGrid<T> extends Composite implements Translatable {
    private final I18N i18n;
    private final String captionId;
    private final Object[] params;
    private final Map<Grid.Column<T, ?>, Pair<String, Object[]>> columnCaptions;
    private final ExposingGrid<T> grid;
    private final QueryDslDataProvider<T> dataProvider;

    public MyGrid(QueryDslDataProvider<T> dataProvider) {
        this(dataProvider, null, null);
    }

    public MyGrid(QueryDslDataProvider<T> dataProvider, I18N i18n, String captionId, Object... params) {
        this.dataProvider = dataProvider;
        grid = new ExposingGrid<>(dataProvider);
        setCompositionRoot(grid);
        setSizeFull();
        MiddleClickExtension.extend(this);
        this.i18n = i18n;
        this.captionId = captionId;
        columnCaptions = new HashMap<>();
        this.params = params;
        if (i18n != null) {
            updateMessageStrings(i18n.getLocale());
        }
    }

    @NonNull
    public <R> Grid.Column<T, R> addColumn(@NonNull ValueProvider<T, R> valueProvider, @NonNull String captionId, Object... params) {
        Grid.Column<T, R> column = addColumn(valueProvider, new TextRenderer(), i18n.get(captionId, params));
        columnCaptions.put(column, Pair.of(captionId, params));
        return column;
    }

    @NonNull
    public <R> Grid.Column<T, R> addColumn(@NonNull ValueProvider<T, R> valueProvider, @NonNull AbstractRenderer<? super T, ? super R> renderer, @NonNull String captionId, Object... params) {
        Grid.Column<T, R> column = addColumn(valueProvider, renderer).setCaption(i18n.get(captionId, params));
        columnCaptions.put(column, Pair.of(captionId, params));
        return column;
    }

    @NonNull
    public <R> Grid.Column<T, R> addColumn(@NonNull ValueProvider<T, R> valueProvider, @NonNull Expression<? extends Comparable> sort, @NonNull String captionId, Object... params) {
        return addColumn(valueProvider, new TextRenderer(), sort, captionId, params);
    }

    @NonNull
    public <R> Grid.Column<T, R> addColumn(@NonNull ValueProvider<T, R> valueProvider, @NonNull AbstractRenderer<? super T, ? super R> renderer,
            @NonNull Expression<? extends Comparable> sort, @NonNull String captionId, Object... params) {
        Grid.Column<T, R> column = grid.addColumn(valueProvider, renderer).setId(dataProvider.addSortable(sort)).setCaption(i18n.get(captionId, params));
        columnCaptions.put(column, Pair.of(captionId, params));
        return column;
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

    public void deselectAll() {
        grid.deselectAll();
    }

    public void addOnClickNavigation(@NonNull NavigationManager navigationManager, Class<? extends BaseView> namedView, Function<Grid.ItemClick<T>, String> parameterGetter) {
        grid.addItemClickListener(e -> {
            boolean newTab = e.getMouseEventDetails().getButton() == MouseEventDetails.MouseButton.MIDDLE || e.getMouseEventDetails().isCtrlKey();
            navigationManager.navigateTo(namedView, parameterGetter.apply(e), newTab);
        });
    }

    public QueryDslDataProvider<T> getDataProvider() {
        return dataProvider;
    }

    @Override
    public void updateMessageStrings(Locale locale) {
        setCaption(i18n.get(captionId, locale, params));
        columnCaptions.forEach((column, caption) -> column.setCaption(i18n.get(caption.getFirst(), locale, caption.getSecond())));
    }

    public static class MiddleClickExtension<T> extends Grid.AbstractGridExtension<T> {
        private MiddleClickExtension(MyGrid<T> myGrid) {
            ExposingGrid<T> grid = myGrid.grid;
            super.extend(grid);
            registerRpc((rowIndex, rowKey, columnInternalId, details) -> grid.fireEvent(new Grid.ItemClick<>(grid,
                    grid.getColumnByInternalId(columnInternalId),
                    grid.getDataCommunicator().getKeyMapper().get(rowKey),
                    details,
                    rowIndex)), GridMiddleClickExtensionConnector.Rpc.class);
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
        ExposingGrid(DataProvider<T, ?> dataProvider) {
            super(dataProvider);
        }

        @Override
        protected Column<T, ?> getColumnByInternalId(String columnId) {
            return super.getColumnByInternalId(columnId);
        }

        @Override
        protected void fireEvent(EventObject event) {
            super.fireEvent(event);
        }
    }
}

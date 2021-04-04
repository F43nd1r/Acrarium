package com.faendir.acra.ui.component.grid;

import com.faendir.acra.dataprovider.QueryDslDataProvider
import com.faendir.acra.settings.GridSettings
import com.faendir.acra.ui.ext.setFlexGrow
import com.faendir.acra.ui.ext.setMarginRight
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import kotlin.reflect.KMutableProperty0

class AcrariumGridView<T>(
    dataProvider: QueryDslDataProvider<T>,
    gridSettings: KMutableProperty0<GridSettings?>,
    initializer: QueryDslAcrariumGrid<T>.() -> Unit = {}
) :
    VerticalLayout() {
    val grid: QueryDslAcrariumGrid<T> = QueryDslAcrariumGrid(dataProvider, gridSettings.get()).apply {
        initializer()
        loadLayout()
        addOnLayoutChangedListener { gridSettings.set(it) }
    }
    val header: HasComponents
    init {
        header = FlexLayout(
            Div().apply { setFlexGrow(1) }, //spacer
            GridFilterMenu(grid).apply { content.setMarginRight(5.0, com.faendir.acra.ui.ext.SizeUnit.PIXEL) },
            GridColumnMenu(grid)
        )
        header.setWidthFull()
        add(header, grid)
        setFlexGrow(1.0, grid)
        setSizeFull()
    }
}

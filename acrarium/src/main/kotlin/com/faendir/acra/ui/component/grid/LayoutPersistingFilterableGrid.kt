package com.faendir.acra.ui.component.grid

import com.faendir.acra.dataprovider.AcrariumDataProvider
import com.faendir.acra.i18n.Messages
import com.faendir.acra.settings.GridSettings
import com.faendir.acra.ui.component.grid.renderer.InteractiveColumnRenderer
import com.faendir.acra.ui.component.grid.renderer.RouteButtonRenderer
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.grid.ItemClickEvent
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.router.RouteParameters

abstract class LayoutPersistingFilterableGrid<T : Any, F : Any, S : Any, C : FilterableSortableLocalizedColumn<T, F, S>>(
    val dataProvider: AcrariumDataProvider<T, F, S>,
    private var gridSettings: GridSettings? = null
) :
    AbstractCustomColumnGrid<T, C>() {

    init {
        dataCommunicator.setDataProvider(dataProvider) { filterableColumns.mapNotNull { it.filter?.invoke() }.toSet() }
        setSizeFull()
        isMultiSort = true
        isColumnReorderingAllowed = true
    }

    final override fun setSizeFull() = super.setSizeFull()

    val filterableColumns: List<FilterableSortableLocalizedColumn<T, F, S>>
        get() = super.getColumns().filterIsInstance<FilterableSortableLocalizedColumn<T, F, S>>()

    /**
     * call when all columns were added
     */
    fun loadLayout() {
        gridSettings?.apply {
            val orderedColumns = columnOrder.mapNotNull { key -> filterableColumns.find { it.key == key } }
            val unorderedColumns = filterableColumns - orderedColumns.toSet()
            setColumnOrder(orderedColumns + unorderedColumns)
            hiddenColumns.forEach { key -> filterableColumns.find { it.key == key }?.isVisible = false }
        }
    }

    fun addOnLayoutChangedListener(listener: (gridSettings: GridSettings) -> Unit) {
        addColumnReorderListener { event ->
            val settings = GridSettings(event.columns.mapNotNull { it.key }, gridSettings?.hiddenColumns ?: emptyList())
            gridSettings = settings
            listener(settings)
        }
        filterableColumns.forEach { column ->
            column.addVisibilityChangeListener {
                val settings = GridSettings(gridSettings?.columnOrder ?: columns.mapNotNull { it.key }, columns.filter { !it.isVisible }.mapNotNull { it.key })
                gridSettings = settings
                listener(settings)
            }
        }
    }

    fun addOnClickNavigation(target: Class<out Component>, getParameters: (T) -> Map<String, String>) {
        addItemClickListener { e: ItemClickEvent<T> ->
            if (e.column.renderer !is InteractiveColumnRenderer) {
                ui.ifPresent { it.navigate(target, RouteParameters(getParameters(e.item))) }
            }
        }
        column(RouteButtonRenderer(VaadinIcon.EXTERNAL_LINK, target, getParameters)) {
            setCaption(Messages.OPEN)
            isAutoWidth = false
            width = "100px"
        }
    }
}
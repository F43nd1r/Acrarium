package com.faendir.acra.ui.component.grid

import com.faendir.acra.util.PARAM
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.ItemClickEvent
import com.vaadin.flow.data.renderer.Renderer
import com.vaadin.flow.function.ValueProvider
import com.vaadin.flow.router.RouteConfiguration
import com.vaadin.flow.router.RouteParameters
import java.util.function.BiFunction
import java.util.function.Consumer

@Suppress("UNCHECKED_CAST")
abstract class AbstractAcrariumGrid<T, C : Grid.Column<T>>() : Grid<T>() {
    abstract val columnFactory: (Renderer<T>, String) -> C

    override fun getDefaultColumnFactory(): BiFunction<Renderer<T>, String, Column<T>> =
        BiFunction { renderer, columnId -> columnFactory(renderer, columnId) }

    override fun addColumn(propertyName: String): C = super.addColumn(propertyName) as C
    override fun addColumn(valueProvider: ValueProvider<T, *>): C = super.addColumn(valueProvider) as C
    override fun addColumn(renderer: Renderer<T>): C = super.addColumn(renderer) as C

    @Deprecated(message = "Not supported")
    override fun <V : Comparable<V>?> addColumn(valueProvider: ValueProvider<T, V>, vararg sortingProperties: String): Column<T> {
        throw UnsupportedOperationException()
    }

    @Deprecated(message = "Not supported")
    override fun addColumn(renderer: Renderer<T>, vararg sortingProperties: String): Column<T> {
        throw UnsupportedOperationException()
    }

    /**
     * workaround https://github.com/vaadin/vaadin-grid/issues/1864
     */
    override fun recalculateColumnWidths() {
        getElement().executeJs("setTimeout(() => { this.recalculateColumnWidths() }, 10)");
    }

    fun <C, R> addOnClickNavigation(target: Class<C>, transform: (T) -> R) where C : Component {
        addItemClickListener { e: ItemClickEvent<T> ->
            ui.ifPresent(if (e.button == 1 || e.isCtrlKey) Consumer {
                it.page.executeJs(
                    """window.open("${
                        RouteConfiguration.forSessionScope().getUrl(target, RouteParameters(PARAM, transform(e.item).toString()))
                    }", "blank", "");"""
                )
            } else Consumer { it.navigate(target, RouteParameters(PARAM, transform(e.item).toString())) })
        }
    }
}

fun <T, C : Grid.Column<T>> AbstractAcrariumGrid<T, C>.column(valueProvider: ValueProvider<T, *>, initializer: C.() -> Unit = {}): C {
    return addColumn(valueProvider).apply(initializer)
}
fun <T, C : Grid.Column<T>> AbstractAcrariumGrid<T, C>.column(renderer: Renderer<T>, initializer: C.() -> Unit = {}): C {
    return addColumn(renderer).apply(initializer)
}
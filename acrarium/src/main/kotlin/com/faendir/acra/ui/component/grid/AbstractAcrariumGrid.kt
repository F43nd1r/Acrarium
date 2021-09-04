package com.faendir.acra.ui.component.grid

import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.data.renderer.Renderer
import com.vaadin.flow.function.ValueProvider
import java.util.function.BiFunction

@Suppress("UNCHECKED_CAST")
abstract class AbstractAcrariumGrid<T, C : Grid.Column<T>> : Grid<T>() {
    abstract val columnFactory: (Renderer<T>, String) -> C

    override fun getDefaultColumnFactory(): BiFunction<Renderer<T>, String, Column<T>> =
        BiFunction { renderer, columnId -> columnFactory(renderer, columnId) }

    override fun addColumn(propertyName: String): C = super.addColumn(propertyName) as C
    override fun addColumn(valueProvider: ValueProvider<T, *>): C = super.addColumn(valueProvider) as C
    override fun addColumn(renderer: Renderer<T>): C = super.addColumn(renderer) as C

    @Deprecated(message = "Not supported")
    override fun <V : Comparable<V>?> addColumn(valueProvider: ValueProvider<T, V>, vararg sortingProperties: String) =
        throw UnsupportedOperationException()

    @Deprecated(message = "Not supported")
    override fun addColumn(renderer: Renderer<T>, vararg sortingProperties: String) = throw UnsupportedOperationException()

    /**
     * workaround https://github.com/vaadin/vaadin-grid/issues/1864
     */
    override fun recalculateColumnWidths() {
        element.executeJs("setTimeout(() => { this.recalculateColumnWidths() }, 10)")
    }
}

fun <T, C : Grid.Column<T>> AbstractAcrariumGrid<T, C>.column(valueProvider: ValueProvider<T, *>, initializer: C.() -> Unit = {}): C {
    return addColumn(valueProvider).apply(initializer)
}

fun <T, C : Grid.Column<T>> AbstractAcrariumGrid<T, C>.column(renderer: Renderer<T>, initializer: C.() -> Unit = {}): C {
    return addColumn(renderer).apply(initializer)
}
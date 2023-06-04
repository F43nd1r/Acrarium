/*
 * (C) Copyright 2022-2023 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.ui.component.grid

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.data.renderer.Renderer
import com.vaadin.flow.function.ValueProvider
import java.util.function.BiFunction

@Suppress("DeprecatedCallableAddReplaceWith")
abstract class AbstractCustomColumnGrid<T, C : Grid.Column<T>> : Grid<T>() {
    abstract val columnFactory: (Renderer<T>, String) -> C

    override fun getDefaultColumnFactory(): BiFunction<Renderer<T>, String, Column<T>> = BiFunction(columnFactory)

    override fun addColumn(valueProvider: ValueProvider<T, *>): C = super.addColumn(valueProvider, columnFactory)
    override fun addColumn(renderer: Renderer<T>): C = super.addColumn(renderer, BiFunction(columnFactory))

    @Deprecated("Not supported")
    override fun addColumn(propertyName: String) = throw UnsupportedOperationException()

    @Deprecated("Not supported")
    override fun addColumns(vararg propertyNames: String) = throw UnsupportedOperationException()

    @Deprecated("Not supported")
    override fun <V : Comparable<V>?> addColumn(valueProvider: ValueProvider<T, V>, vararg sortingProperties: String) = throw UnsupportedOperationException()

    @Deprecated("Not supported")
    override fun <V : Component?> addComponentColumn(componentProvider: ValueProvider<T, V>?) = throw UnsupportedOperationException()
}

fun <T, C : Grid.Column<T>> AbstractCustomColumnGrid<T, C>.column(valueProvider: ValueProvider<T, *>, initializer: C.() -> Unit = {}): C {
    return addColumn(valueProvider).apply(initializer)
}

fun <T, C : Grid.Column<T>> AbstractCustomColumnGrid<T, C>.column(renderer: Renderer<T>, initializer: C.() -> Unit = {}): C {
    return addColumn(renderer).apply(initializer)
}
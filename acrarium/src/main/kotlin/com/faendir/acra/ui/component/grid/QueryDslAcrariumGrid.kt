/*
 * (C) Copyright 2020 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.component.grid

import com.faendir.acra.dataprovider.QueryDslDataProvider
import com.faendir.acra.i18n.Messages
import com.faendir.acra.settings.GridSettings
import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.BooleanExpression
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.grid.ItemClickEvent
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.data.renderer.Renderer
import com.vaadin.flow.router.RouteParameters

/**
 * @author lukas
 * @since 13.07.18
 */
class QueryDslAcrariumGrid<T : Any>(dataProvider: QueryDslDataProvider<T>, gridSettings: GridSettings? = null) :
    LayoutPersistingFilterableGrid<T, BooleanExpression, Expression<out Comparable<*>>, QueryDslAcrariumColumn<T>>(dataProvider, gridSettings) {
    override val columnFactory: (Renderer<T>, String) -> QueryDslAcrariumColumn<T> = { renderer, id -> QueryDslAcrariumColumn(this, renderer, id) }

}
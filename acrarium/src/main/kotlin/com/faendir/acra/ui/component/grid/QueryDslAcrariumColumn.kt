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

import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.StringExpression
import com.vaadin.flow.data.renderer.Renderer

class QueryDslAcrariumColumn<T : Any>(acrariumGrid: QueryDslAcrariumGrid<T>, renderer: Renderer<T>, columnId: String) :
    FilterableSortableLocalizedColumn<T, BooleanExpression, Expression<out Comparable<*>>>(acrariumGrid, renderer, columnId) {

    fun setFilterable(expr: StringExpression, captionId: String, vararg params: Any) {
        setFilterable({ expr.contains(it) }, captionId, params)
    }

    fun setSortableAndFilterable(expr: StringExpression, captionId: String, vararg params: Any) {
        setSortable(expr)
        setFilterable(expr, captionId, params)
    }

}
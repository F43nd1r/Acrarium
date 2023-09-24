/*
 * (C) Copyright 2023 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.common

import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.grid.LocalizedColumn
import com.github.mvysny.kaributesting.v10.SearchSpec
import com.vaadin.flow.component.grid.Grid.Column
import com.vaadin.flow.data.renderer.Renderer
import java.util.function.Predicate
import kotlin.reflect.KClass

@get:JvmName("translatableCaptionId")
@set:JvmName("translatableCaptionId")
var <T : Translatable<*>> SearchSpec<T>.captionId: String
    @Deprecated("", level = DeprecationLevel.ERROR) get() = throw UnsupportedOperationException()
    set(value) {
        predicates.add(object : Predicate<T> {
            override fun test(t: T) = t.captionId == value
            override fun toString() = "captionId==$value"
        })
    }

@get:JvmName("localizedColumnCaptionId")
@set:JvmName("localizedColumnCaptionId")
var <T : LocalizedColumn<*>> SearchSpec<T>.captionId: String
    @Deprecated("", level = DeprecationLevel.ERROR) get() = throw UnsupportedOperationException()
    set(value) {
        predicates.add(object : Predicate<T> {
            override fun test(t: T) = t.captionId == value
            override fun toString() = "captionId==$value"
        })
    }

fun <T : Column<*>> SearchSpec<T>.rendererIs(renderer: KClass<out Renderer<*>>) {
    predicates.add(object : Predicate<T> {
        override fun test(t: T) = renderer.isInstance(t.renderer)
        override fun toString() = "renderer is $renderer"
    })
}

@Suppress("UNCHECKED_CAST")
fun <T> Column<*>.editorComponent(): T = editorComponent as T
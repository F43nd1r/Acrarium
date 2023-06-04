/*
 * (C) Copyright 2022 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.component.grid.renderer

import com.faendir.acra.persistence.version.VersionKey
import com.faendir.acra.persistence.version.VersionName
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.data.renderer.ComponentRenderer

class VersionRenderer<T>(versions: Collection<VersionName>, valueProvider: (T) -> VersionKey?) : ComponentRenderer<Span, T>({ t: T ->
    valueProvider(t)?.let { (code, flavor) ->
        Span(versions.first { code == it.code && flavor == it.flavor }.name).apply {
            element.setProperty("title", code.toString() + (flavor.takeIf { it.isNotBlank() }?.let { " $it" } ?: ""))
        }
    } ?: Span()
})
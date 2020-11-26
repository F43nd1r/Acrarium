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
package com.faendir.acra.util

import com.vaadin.flow.component.html.Span
import com.vaadin.flow.data.renderer.ComponentRenderer
import org.xbib.time.pretty.PrettyTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * @author Lukas
 * @since 26.05.2017
 */
class TimeSpanRenderer<T>(valueProvider: (T) -> ZonedDateTime?) : ComponentRenderer<Span, T>({ t: T ->
    valueProvider(t)?.let {
        Span(PrettyTime(Locale.US).formatUnrounded(it.toLocalDateTime())).apply {
            element.setProperty("title", DateTimeFormatter.ISO_DATE_TIME.format(it))
        }
    } ?: Span()
})
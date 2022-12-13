package com.faendir.acra.ui.component.grid.renderer

import com.faendir.acra.persistence.version.VersionName
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.data.renderer.ComponentRenderer

class VersionRenderer<T>(versions: Collection<VersionName>, valueProvider: (T) -> Pair<Int, String>?) : ComponentRenderer<Span, T>({ t: T ->
    valueProvider(t)?.let { (code, flavor) ->
        Span(versions.first { code == it.code && flavor == it.flavor }.name).apply {
            element.setProperty("title", code.toString() + (flavor.takeIf { it.isNotBlank() }?.let { " $it" } ?: ""))
        }
    } ?: Span()
})
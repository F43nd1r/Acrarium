package com.faendir.acra.ui.component.grid

import com.vaadin.flow.data.renderer.Renderer

class BasicCustomColumnGrid<T> : AbstractCustomColumnGrid<T, LocalizedColumn<T>>() {
    override val columnFactory: (Renderer<T>, String) -> LocalizedColumn<T> = { renderer, id -> LocalizedColumn(this, renderer, id) }
}
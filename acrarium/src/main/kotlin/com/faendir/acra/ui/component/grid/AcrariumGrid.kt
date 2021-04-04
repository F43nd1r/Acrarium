package com.faendir.acra.ui.component.grid

import com.vaadin.flow.data.renderer.Renderer

class AcrariumGrid<T> : AbstractAcrariumGrid<T, AcrariumColumn<T>>() {
    override val columnFactory: (Renderer<T>, String) -> AcrariumColumn<T> = { renderer, id -> AcrariumColumn(this, renderer, id) }
}
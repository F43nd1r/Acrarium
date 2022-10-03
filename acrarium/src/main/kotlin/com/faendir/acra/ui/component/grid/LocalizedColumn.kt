package com.faendir.acra.ui.component.grid

import com.faendir.acra.i18n.TranslatableText
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.data.renderer.Renderer
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver

open class LocalizedColumn<T>(grid: Grid<T>, renderer: Renderer<T>, columnId: String) :
    Grid.Column<T>(grid, columnId, renderer),
    LocaleChangeObserver {
    var caption: TranslatableText? = null
        set(value) {
            field = value
            key = value?.let { it.id + it.params.joinToString("") { param -> param.toString() } }
        }
    var captionId: String?
        get() = caption?.id
        set(value) {
            caption = value?.let { TranslatableText(it) }
        }
    private val visibilityChangeListeners = mutableListOf<() -> Unit>()

    init {
        isResizable = true
        isAutoWidth = true
        flexGrow = 0
    }

    override fun setVisible(visible: Boolean) {
        val oldValue = isVisible
        super.setVisible(visible)
        if (oldValue != visible) {
            visibilityChangeListeners.forEach { it() }
        }
    }

    fun addVisibilityChangeListener(listener: () -> Unit) {
        visibilityChangeListeners.add(listener)
    }

    fun setCaption(id: String, vararg params: Any) { caption = TranslatableText(id, *params) }

    override fun localeChange(event: LocaleChangeEvent?) {
        caption?.let { setHeader(it.translate()) }
    }
}
package com.faendir.acra.ui.component.dialog

import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.util.toNullable
import com.vaadin.flow.component.ClickEvent
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.dom.Element
import kotlin.streams.asSequence

open class AcrariumDialog private constructor(private val content: DialogContent) : Dialog(), HasComponents by content {
     constructor() : this(DialogContent())

    init {
        super<Dialog>.add(content)
    }

    fun setHeader(captionId: String, vararg params: Any) {
        content.setHeader(Translatable.createH3(captionId, *params))
    }

    fun setPositive(captionId: String, vararg params: Any, clickListener: (ClickEvent<Button>) -> Unit = {}) {
        val button = Translatable.createButton(captionId, *params) {
            close()
            clickListener(it)
        }
        content.setPositive(button)
    }

    val positive: Translatable<Button>?
        get() = content.element.children.asSequence().filter { it.getAttribute("slot") == "positive" }.map { it.component.toNullable() }.filterIsInstance<Translatable<Button>>().firstOrNull()

    fun setNegative(captionId: String, vararg params: Any, clickListener: (ClickEvent<Button>) -> Unit = {}) {
        val button = Translatable.createButton(captionId, *params) {
            close()
            clickListener(it)
        }.with {
            removeThemeVariants(ButtonVariant.LUMO_PRIMARY)
            addThemeVariants(ButtonVariant.LUMO_TERTIARY)
        }
        content.setNegative(button)
    }

    override fun getElement(): Element {
        return super.getElement()
    }
}
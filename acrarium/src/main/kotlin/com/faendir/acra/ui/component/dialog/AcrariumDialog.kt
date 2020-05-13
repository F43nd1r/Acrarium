package com.faendir.acra.ui.component.dialog

import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.util.toNullable
import com.vaadin.flow.component.ClickEvent
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.dom.Element
import kotlin.streams.asSequence

open class AcrariumDialog private constructor(private val dialogContent: DialogContent) : Composite<Dialog>(), HasComponents {
    constructor() : this(DialogContent())

    init {
        content.add(dialogContent)
    }

    var isOpened: Boolean
        get() = content.isOpened
        set(value) {
            content.isOpened = value
        }

    fun open() {
        content.open()
    }

    fun close() {
        content.close()
    }

    fun setHeader(captionId: String, vararg params: Any) {
        dialogContent.setHeader(Translatable.createH3(captionId, *params))
    }

    fun setPositive(captionId: String, vararg params: Any, clickListener: (ClickEvent<Button>) -> Unit = {}) {
        val button = Translatable.createButton(captionId, *params) {
            close()
            clickListener(it)
        }
        dialogContent.setPositive(button)
    }

    val positive: Translatable<Button>?
        get() = dialogContent.element.children.asSequence()
                .filter { it.getAttribute("slot") == "positive" }
                .map { it.component.toNullable() }
                .filterIsInstance<Translatable<Button>>()
                .firstOrNull()

    fun setNegative(captionId: String, vararg params: Any, clickListener: (ClickEvent<Button>) -> Unit = {}) {
        val button = Translatable.createButton(captionId, *params) {
            close()
            clickListener(it)
        }.with {
            removeThemeVariants(ButtonVariant.LUMO_PRIMARY)
            addThemeVariants(ButtonVariant.LUMO_TERTIARY)
        }
        dialogContent.setNegative(button)
    }

    override fun removeAll() {
        dialogContent.removeAll()
    }

    override fun addComponentAsFirst(component: Component?) {
        dialogContent.addComponentAsFirst(component)
    }

    override fun add(vararg components: Component?) {
        dialogContent.add(*components)
    }

    override fun add(text: String?) {
        dialogContent.add(text)
    }

    override fun remove(vararg components: Component?) {
        dialogContent.remove(*components)
    }

    override fun addComponentAtIndex(index: Int, component: Component?) {
        dialogContent.addComponentAtIndex(index, component)
    }
}
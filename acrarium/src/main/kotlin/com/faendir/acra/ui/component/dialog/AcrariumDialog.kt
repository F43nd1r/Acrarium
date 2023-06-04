/*
 * (C) Copyright 2019-2021 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.component.dialog

import com.faendir.acra.i18n.Messages
import com.faendir.acra.ui.component.Translatable
import com.vaadin.flow.component.ClickEvent
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dialog.Dialog

open class AcrariumDialog : Composite<Dialog>(), HasComponents {
    private val dialogContent: DialogContent = DialogContent()

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

    fun header(captionId: String, vararg params: Any) {
        dialogContent.add(DialogContent.Slot.HEADER, Translatable.createH3(captionId, *params))
    }

    fun positiveAction(captionId: String, vararg params: Any, clickListener: (ClickEvent<Button>) -> Unit = {}) {
        dialogContent.add(DialogContent.Slot.POSITIVE, Translatable.createButton(captionId, *params) {
            close()
            clickListener(it)
        })
    }

    val positive: Translatable<Button>?
        get() = dialogContent.get(DialogContent.Slot.POSITIVE)
            .filterIsInstance<Translatable<Button>>()
            .firstOrNull()

    fun negativeAction(captionId: String, vararg params: Any, clickListener: (ClickEvent<Button>) -> Unit = {}) {
        dialogContent.add(DialogContent.Slot.NEGATIVE, Translatable.createButton(captionId, *params, theme = ButtonVariant.LUMO_TERTIARY) {
            close()
            clickListener(it)
        })
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

    override fun removeAll() {
        dialogContent.removeAll()
    }

    override fun addComponentAtIndex(index: Int, component: Component?) {
        dialogContent.addComponentAtIndex(index, component)
    }

    override fun addComponentAsFirst(component: Component?) {
        dialogContent.addComponentAsFirst(component)
    }
}

fun AcrariumDialog.createButton(onCreateAction: AcrariumDialog.() -> Unit) {
    positiveAction(Messages.CREATE) { onCreateAction() }
    negativeAction(Messages.CANCEL)
}

fun AcrariumDialog.closeButton() {
    positiveAction(Messages.CLOSE)
}

fun AcrariumDialog.confirmButtons(onYesAction: AcrariumDialog.() -> Unit) {
    positiveAction(Messages.CONFIRM) { onYesAction() }
    negativeAction(Messages.CANCEL)
}
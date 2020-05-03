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
package com.faendir.acra.ui.component

import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent
import com.vaadin.flow.component.ClickEvent
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.ComponentEvent
import com.vaadin.flow.component.ComponentEventListener
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.HasSize
import com.vaadin.flow.component.HasStyle
import com.vaadin.flow.component.HasText
import com.vaadin.flow.component.HasValidation
import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.Text
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.NumberField
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.shared.Registration

/**
 * @author lukas
 * @since 14.11.18
 */
open class Translatable<T : Component> protected constructor(protected val t: T, private val setter: T.() -> Unit) :
        Composite<T>(), LocaleChangeObserver, HasSize, HasStyle {
    constructor(t: T, property: T.(String) -> Unit, captionId: String, vararg params: Any) : this(t, { t.property(t.getTranslation(captionId, *params)) })

    override fun initContent(): T {
        return t
    }

    override fun localeChange(event: LocaleChangeEvent) {
        t.setter()
        fireEvent(TranslatedEvent(this, false))
    }

    open fun with(consumer: T.() -> Unit): Translatable<T> {
        t.consumer()
        return this
    }

    fun addTranslatedListener(listener: (TranslatedEvent) -> Unit): Registration {
        return addListener(TranslatedEvent::class.java, listener)
    }

    open class Value<T, E : ComponentValueChangeEvent<in T, V>, V>(t: T, setter: T.() -> Unit) : Translatable<T>(t, setter), HasValue<E, V> by t
            where T : Component, T : HasValue<E, V> {
        constructor(t: T, property: T.(String) -> Unit, captionId: String, vararg params: Any) : this(t, { t.property(t.getTranslation(captionId, *params)) })

        override fun with(consumer: T.() -> Unit): Value<T, E, V> {
            t.consumer()
            return this
        }
    }

    class ValidatedValue<T, E : ComponentValueChangeEvent<in T, V>, V>(t: T, setter: T.() -> Unit) : Value<T, E, V>(t, setter), HasValidation by t
            where T : Component, T : HasValue<E, V>, T : HasValidation {
        constructor(t: T, property: T.(String) -> Unit, captionId: String, vararg params: Any) : this(t, { t.property(t.getTranslation(captionId, *params)) })

        override fun with(consumer: T.() -> Unit): ValidatedValue<T, E, V> {
            t.consumer()
            return this
        }
    }

    class TranslatedEvent(source: Translatable<*>, fromClient: Boolean) : ComponentEvent<Translatable<*>>(source, fromClient)
    companion object {

        fun createText(captionId: String, vararg params: Any) = Translatable(Text(""), HasText::setText, captionId, *params)

        fun createButton(captionId: String, vararg params: Any, clickListener: ((ClickEvent<Button>) -> Unit)? = null) =
                Translatable(Button("", clickListener).apply { addThemeVariants(ButtonVariant.LUMO_PRIMARY) }, HasText::setText, captionId, *params)

        fun createTextField(captionId: String, vararg params: Any) = ValidatedValue(TextField(), TextField::setLabel, captionId, *params)

        fun createTextFieldWithHint(captionId: String, vararg params: Any) = ValidatedValue(TextField(), TextField::setPlaceholder, captionId, *params)

        fun createPasswordField(captionId: String, vararg params: Any) = ValidatedValue(PasswordField(), PasswordField::setLabel, captionId, *params)

        fun createTextArea(captionId: String, vararg params: Any) = ValidatedValue(TextArea(), TextArea::setLabel, captionId, *params)

        fun <T> createComboBox(items: Collection<T>, captionId: String, vararg params: Any) = ValidatedValue(ComboBox("", items), ComboBox<T>::setLabel, captionId, *params)

        fun <T> createSelect(items: Collection<T>, captionId: String, vararg params: Any) = Translatable(Select<T>().apply { setItems(items) }, {
            label = getTranslation(captionId, *params)
            setItemLabelGenerator(itemLabelGenerator)
        })

        fun createCheckbox(captionId: String, vararg params: Any) = Value(Checkbox(), Checkbox::setLabel, captionId, *params)

        fun createLabel(captionId: String, vararg params: Any) = Translatable(Label(), HasText::setText, captionId, *params)

        fun createH3(captionId: String, vararg params: Any) = Translatable(H3(), HasText::setText, captionId, *params)

        fun createImage(src: String, captionId: String, vararg params: Any) = Translatable(Image(src, ""), Image::setAlt, captionId, *params)

        fun createNumberField(captionId: String, vararg params: Any) = ValidatedValue(NumberField(), NumberField::setLabel, captionId, *params)

        fun createNumberFieldWithHint(captionId: String, vararg params: Any) = ValidatedValue(NumberField(), NumberField::setPlaceholder, captionId, *params)

        fun createUploadField(captionId: String, vararg params: Any) = ValidatedValue(UploadField(), UploadField::setLabel, captionId, *params)

        fun createDiv(captionId: String, vararg params: Any) = Translatable(Div(), Div::setText, captionId, *params)

        fun createRangeField(captionId: String, vararg params: Any) = ValidatedValue(RangeField(), RangeField::setLabel, captionId, *params)
    }
}
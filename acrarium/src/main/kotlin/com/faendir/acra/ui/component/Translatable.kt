/*
 * (C) Copyright 2018-2023 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.component

import com.faendir.acra.ui.ext.label
import com.vaadin.flow.component.*
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.html.*
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.component.textfield.NumberField
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.router.RouteParameters
import com.vaadin.flow.router.RouterLink
import com.vaadin.flow.shared.Registration
import kotlin.reflect.KClass

/**
 * @author lukas
 * @since 14.11.18
 */
open class Translatable<T : Component>(protected val t: T, private val property: T.(String) -> Unit, internal val captionId: String, private vararg val params: Any) :
    Composite<T>(), LocaleChangeObserver, HasSize, HasStyle {

    override fun initContent(): T {
        return t
    }

    override fun localeChange(event: LocaleChangeEvent) {
        t.property(t.getTranslation(captionId, *params))
        fireEvent(TranslatedEvent(this, false))
    }

    open fun with(consumer: T.() -> Unit): Translatable<T> {
        t.consumer()
        return this
    }

    override fun isVisible() = when (t) {
        is Text -> !t.text.isNullOrBlank() // workaround for https://github.com/vaadin/flow/issues/3201
        else -> t.isVisible
    }

    fun addTranslatedListener(listener: (TranslatedEvent) -> Unit): Registration {
        return addListener(TranslatedEvent::class.java, listener)
    }

    open class Value<T, E : ComponentValueChangeEvent<in T, V>, V>(t: T, property: T.(String) -> Unit, captionId: String, vararg params: Any) :
        Translatable<T>(t, property, captionId, *params), HasValue<E, V> by t
            where T : Component, T : HasValue<E, V> {

        override fun with(consumer: T.() -> Unit): Value<T, E, V> {
            t.consumer()
            return this
        }
    }

    class ValidatedValue<T, E : ComponentValueChangeEvent<in T, V>, V>(t: T, property: T.(String) -> Unit, captionId: String, vararg params: Any) :
        Value<T, E, V>(t, property, captionId, *params), HasValidation by t
            where T : Component, T : HasValue<E, V>, T : HasValidation {

        override fun with(consumer: T.() -> Unit): ValidatedValue<T, E, V> {
            t.consumer()
            return this
        }
    }

    class TranslatedEvent(source: Translatable<*>, fromClient: Boolean) : ComponentEvent<Translatable<*>>(source, fromClient)
    companion object {

        fun createText(captionId: String, vararg params: Any) = Translatable(Text(""), HasText::setText, captionId, *params)

        fun createButton(
            captionId: String,
            vararg params: Any,
            theme: ButtonVariant = ButtonVariant.LUMO_PRIMARY,
            clickListener: (ClickEvent<Button>) -> Unit = {}
        ) = Translatable(Button("", clickListener).apply { addThemeVariants(theme) }, HasText::setText, captionId, *params)

        fun createTextField(captionId: String, vararg params: Any) = ValidatedValue(TextField(), TextField::setLabel, captionId, *params)

        fun createTextFieldWithHint(captionId: String, vararg params: Any) = ValidatedValue(TextField(), TextField::setPlaceholder, captionId, *params)

        fun createPasswordField(captionId: String, vararg params: Any) = ValidatedValue(PasswordField(), PasswordField::setLabel, captionId, *params)

        fun createTextArea(captionId: String, vararg params: Any) = ValidatedValue(TextArea(), TextArea::setLabel, captionId, *params)

        fun <T> createComboBox(items: Collection<T>, captionId: String, vararg params: Any) =
            ValidatedValue(ComboBox("", items), ComboBox<T>::setLabel, captionId, *params)

        fun <T> createSelect(items: Collection<T>, getLabel: (T) -> String, captionId: String, vararg params: Any) = Value(Select<T>().apply {
            setItems(items)
        }, Select<*>::label, captionId, params).apply {
            addTranslatedListener {
                t.setItemLabelGenerator(getLabel)
            }
        }

        fun createCheckbox(captionId: String, vararg params: Any) = Value(Checkbox(), Checkbox::setLabel, captionId, *params)

        fun createLabel(captionId: String, vararg params: Any) = Translatable(Label(), HasText::setText, captionId, *params)

        fun createH3(captionId: String, vararg params: Any) = Translatable(H3(), HasText::setText, captionId, *params)

        fun createImage(src: String, captionId: String, vararg params: Any) = Translatable(Image(src, ""), Image::setAlt, captionId, *params)

        fun createNumberField(captionId: String, vararg params: Any) = ValidatedValue(NumberField(), NumberField::setLabel, captionId, *params)

        fun createNumberFieldWithHint(captionId: String, vararg params: Any) = ValidatedValue(NumberField(), NumberField::setPlaceholder, captionId, *params)

        fun createUploadField(captionId: String, vararg params: Any) = ValidatedValue(UploadField(), UploadField::setLabel, captionId, *params)

        fun createDiv(captionId: String, vararg params: Any) = Translatable(Div(), Div::setText, captionId, *params)

        fun createRangeField(captionId: String, vararg params: Any) = ValidatedValue(RangeField(), RangeField::setLabel, captionId, *params)

        fun createP(captionId: String, vararg params: Any) = Translatable(Paragraph(), Paragraph::setText, captionId, *params)

        fun createRouterLink(target: KClass<out Component>, targetParams: Map<String, String> = emptyMap(), captionId: String, vararg params: Any) =
            createRouterLink(target, RouteParameters(targetParams), captionId, *params)

        fun createRouterLink(target: KClass<out Component>, targetParams: RouteParameters, captionId: String, vararg params: Any) =
            Translatable(RouterLink("", target.java, targetParams), HasText::setText, captionId, *params)
    }
}
/*
 * (C) Copyright 2019 Lukas Morawietz (https://github.com/F43nd1r)
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

import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.User
import com.faendir.acra.service.UserService
import com.faendir.acra.ui.component.Translatable.ValidatedValue
import com.faendir.acra.ui.ext.setMaxWidthFull
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.HasValidation
import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.Setter
import com.vaadin.flow.data.binder.ValidationResult
import com.vaadin.flow.data.binder.ValueContext
import com.vaadin.flow.data.validator.EmailValidator
import com.vaadin.flow.dom.ElementFactory
import com.vaadin.flow.function.ValueProvider

/**
 * @author lukas
 * @since 28.02.19
 */
class UserEditor(userService: UserService, u: User?, onSuccess: () -> Unit) : Composite<FlexLayout>() {
    private var user: User = (u ?: User("", "", mutableSetOf(User.Role.ADMIN, User.Role.USER)))

    init {
        val binder = Binder<User>()
        val username = Translatable.createTextField(Messages.USERNAME)
        exposeInput(username)
        username.setWidthFull()
        val usernameBindingBuilder = binder.forField(username)
        if (u == null) {
            usernameBindingBuilder.asRequired(getTranslation(Messages.USERNAME_REQUIRED))
        }
        usernameBindingBuilder.withValidator({ it == user.username || userService.getUser(it) == null }, getTranslation(Messages.USERNAME_TAKEN))
                .bind(ValueProvider { it.username }, if (u == null) Setter { user: User, value: String -> user.username = value } else null)
        content.add(username)
        val mail = Translatable.createTextField(Messages.EMAIL)
        mail.setWidthFull()
        val emailValidator = EmailValidator(getTranslation(Messages.INVALID_MAIL))
        binder.forField(mail).withValidator { m: String, c: ValueContext? -> if (m.isEmpty()) ValidationResult.ok() else emailValidator.apply(m, c) }
                .bind({ it.mail ?: ""}) { user: User, value: String? -> user.mail = value }
        content.add(mail)
        val newPassword = Translatable.createPasswordField(Messages.NEW_PASSWORD)
        exposeInput(newPassword)
        newPassword.setWidthFull()
        val repeatPassword = Translatable.createPasswordField(Messages.REPEAT_PASSWORD)
        exposeInput(repeatPassword)
        repeatPassword.setWidthFull()
        if (u != null) {
            val oldPassword = Translatable.createPasswordField(Messages.OLD_PASSWORD)
            exposeInput(oldPassword)
            oldPassword.setWidthFull()
            val oldPasswordBinding = binder.forField(oldPassword).withValidator({
                if (newPassword.value.isNotEmpty() || oldPassword.value.isNotEmpty()) userService.checkPassword(user, it) else true
            }, getTranslation(Messages.INCORRECT_PASSWORD)).bind({ "" }) { _: User?, _: String? -> }
            content.add(oldPassword)
            newPassword.addValueChangeListener { oldPasswordBinding.validate() }
            repeatPassword.addValueChangeListener { oldPasswordBinding.validate() }
        }
        val newPasswordBindingBuilder = binder.forField(newPassword)
        val repeatPasswordBindingBuilder = binder.forField(repeatPassword)
        if (u == null) {
            newPasswordBindingBuilder.asRequired(getTranslation(Messages.PASSWORD_REQUIRED))
            repeatPasswordBindingBuilder.asRequired(getTranslation(Messages.PASSWORD_REQUIRED))
        }
        newPasswordBindingBuilder.bind({ "" }) { user: User, plainTextPassword: String? ->
            if (plainTextPassword != null && "" != plainTextPassword) {
                user.setPlainTextPassword(plainTextPassword)
            }
        }
        content.add(newPassword)
        val repeatPasswordBinding = repeatPasswordBindingBuilder.withValidator({ it == newPassword.value }, getTranslation(Messages.PASSWORDS_NOT_MATCHING))
                .bind({ "" }) { _: User?, _: String? -> }
        newPassword.addValueChangeListener { if (repeatPassword.value.isNotEmpty()) repeatPasswordBinding.validate() }
        content.add(repeatPassword)
        binder.readBean(user)
        val button = Translatable.createButton(Messages.CONFIRM) {
            if (binder.writeBeanIfValid(user)) {
                user = userService.store(user)
                binder.readBean(user)
                onSuccess()
            }
        }
        button.setWidthFull()
        button.content.isEnabled = false
        binder.addStatusChangeListener { button.content.isEnabled = it.binder.hasChanges() }
        content.add(button)
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN)
        content.setMaxWidthFull()
        content.width = "calc(var(--lumo-size-m) * 10)"
    }

    /**
     * password managers need an input outside the shadow dom, which we add here.
     * @param field the field which should be visible to password managers
     * @param <T> the type of the field
     **/
    private fun <T, V> exposeInput(field: ValidatedValue<T, *, V>) where T : Component, T : HasValue<ComponentValueChangeEvent<T, V>, V>, T : HasValidation {
        val input = ElementFactory.createInput()
        input.setAttribute("slot", "input")
        field.getElement().appendChild(input)
    }
}